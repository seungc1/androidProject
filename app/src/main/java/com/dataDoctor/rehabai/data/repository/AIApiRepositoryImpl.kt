package com.dataDoctor.rehabai.data.repository

import com.dataDoctor.rehabai.data.ExerciseCatalog
import com.dataDoctor.rehabai.data.network.model.* // GptDtos.kt 파일에 정의된 클래스들
import com.dataDoctor.rehabai.domain.model.AIAnalysisResult
import com.dataDoctor.rehabai.domain.model.RehabData
import com.dataDoctor.rehabai.domain.model.AIRecommendationResult
import com.dataDoctor.rehabai.domain.model.RecommendationParams
import com.dataDoctor.rehabai.domain.model.ScheduledWorkout
import com.dataDoctor.rehabai.domain.model.ScheduledDiet
import com.dataDoctor.rehabai.domain.repository.AIApiRepository
import com.dataDoctor.rehabai.data.network.GptApiService
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay
import javax.inject.Inject
import android.util.Log

class AIApiRepositoryImpl @Inject constructor(
    private val gptApiService: GptApiService,
    private val gson: Gson
) : AIApiRepository {

    override suspend fun getAIRehabAndDietRecommendation(params: RecommendationParams): Flow<AIRecommendationResult> = flow {
        // ★★★ 토큰 제한 해결: 운동과 식단을 별도로 요청 ★★★

        try {
            // 1. 운동 계획 요청
            val workoutsResult = fetchWorkouts(params)
            Log.d("AIApiRepo", "운동 계획 수신 완료: ${workoutsResult.size}일치")

            // 2. 식단 계획 요청
            val dietsResult = fetchDiets(params)
            Log.d("AIApiRepo", "식단 계획 수신 완료: ${dietsResult.size}일치")

            // 3. 결과 합치기
            emit(AIRecommendationResult(
                scheduledWorkouts = workoutsResult,
                scheduledDiets = dietsResult,
                overallSummary = "AI 맞춤 재활 및 식단 계획이 생성되었습니다.",
                disclaimer = "본 추천은 AI에 의해 생성되었으며, 전문 의료인의 진단 및 조언을 대체할 수 없습니다."
            ))

        } catch (e: Exception) {
            Log.e("AIApiRepo", "AI 추천 생성 실패: ${e.message}")
            // 🚨 [수정] Unresolved reference 오류 해결을 위해 함수 호출
            emit(createErrorResult("AI 추천을 생성하는 데 실패했습니다. (오류: ${e.message})"))
        }
    }

    // ★★★ 운동 계획만 요청하는 함수 ★★★
    private suspend fun fetchWorkouts(params: RecommendationParams): List<ScheduledWorkout> {
        val systemPrompt = createWorkoutSystemPrompt()
        val userPrompt = createWorkoutUserPrompt(params)

        val request = GptRequest(
            model = "gpt-4-turbo", // [수정] gpt-4-turbo 사용
            messages = listOf(
                GptMessage(role = "system", content = systemPrompt),
                GptMessage(role = "user", content = userPrompt)
            ),
            response_format = ResponseFormat(type = "json_object"),
            max_tokens = 3000
        )

        val MAX_RETRIES = 3
        var delayTime = 1000L
        var gptResponse: GptResponse? = null
        var lastException: Exception? = null

        for (attempt in 1..MAX_RETRIES) {
            try {
                gptResponse = gptApiService.getChatCompletion(request = request)
                Log.d("AIApiRepo", "운동 API 요청 성공 (시도 $attempt)")
                break
            } catch (e: Exception) {
                lastException = e
                Log.w("AIApiRepo", "운동 API 요청 실패 (시도 $attempt/$MAX_RETRIES): ${e.message}")
                if (attempt == MAX_RETRIES) {
                    Log.e("AIApiRepo", "운동 API 요청 최종 실패: ${e.message}")
                    throw e
                }
                delay(delayTime)
                delayTime *= 2
            }
        }

        val jsonResponseString = gptResponse?.choices?.firstOrNull()?.message?.content
        // 🚨 [수정] 널 안정성 강화: 널이면 즉시 예외 발생
            ?: throw Exception("운동 API 응답이 비어있습니다.")

        Log.d("AIApiRepo", "운동 Raw JSON Response: $jsonResponseString")
        val cleanJson = jsonResponseString.replace(Regex("^```json\\s*|\\s*```$"), "").trim()

        return parseWorkoutsResponse(cleanJson)
    }

    // ★★★ 식단 계획만 요청하는 함수 ★★★
    private suspend fun fetchDiets(params: RecommendationParams): List<ScheduledDiet> {
        val systemPrompt = createDietSystemPrompt()
        val userPrompt = createDietUserPrompt(params)

        val request = GptRequest(
            model = "gpt-4-turbo", // [수정] gpt-4-turbo 사용
            messages = listOf(
                GptMessage(role = "system", content = systemPrompt),
                GptMessage(role = "user", content = userPrompt)
            ),
            response_format = ResponseFormat(type = "json_object"),
            max_tokens = 4000
        )

        val MAX_RETRIES = 3
        var delayTime = 1000L
        var gptResponse: GptResponse? = null
        var lastException: Exception? = null

        for (attempt in 1..MAX_RETRIES) {
            try {
                gptResponse = gptApiService.getChatCompletion(request = request)
                Log.d("AIApiRepo", "식단 API 요청 성공 (시도 $attempt)")
                break
            } catch (e: Exception) {
                lastException = e
                Log.w("AIApiRepo", "식단 API 요청 실패 (시도 $attempt/$MAX_RETRIES): ${e.message}")
                if (attempt == MAX_RETRIES) {
                    Log.e("AIApiRepo", "식단 API 요청 최종 실패: ${e.message}")
                    throw e
                }
                delay(delayTime)
                delayTime *= 2
            }
        }

        val jsonResponseString = gptResponse?.choices?.firstOrNull()?.message?.content
        // 🚨 [수정] 널 안정성 강화: 널이면 즉시 예외 발생
            ?: throw Exception("식단 API 응답이 비어있습니다.")

        Log.d("AIApiRepo", "식단 Raw JSON Response: $jsonResponseString")
        val cleanJson = jsonResponseString.replace(Regex("^```json\\s*|\\s*```$"), "").trim()

        return parseDietsResponse(cleanJson)
    }

    override suspend fun analyzeRehabProgress(rehabData: RehabData): Flow<AIAnalysisResult> = flow {
        val systemPrompt = createAnalysisSystemPrompt()
        val userPrompt = createAnalysisUserPrompt(rehabData)

        val request = GptRequest(
            model = "gpt-4-turbo", // [수정] gpt-4-turbo 사용
            messages = listOf(
                GptMessage(role = "system", content = systemPrompt),
                GptMessage(role = "user", content = userPrompt)
            ),
            response_format = ResponseFormat(type = "json_object")
        )

        // ★★★ 429 오류 해결을 위한 재시도 로직 (analyzeProgress) ★★★
        val MAX_RETRIES = 3
        var delayTime = 1000L
        var gptResponse: GptResponse? = null
        var lastException: Exception? = null

        for (attempt in 1..MAX_RETRIES) {
            try {
                gptResponse = gptApiService.getChatCompletion(request = request)
                Log.d("AIApiRepo", "AI 분석 요청 성공 (시도 $attempt)")
                break
            } catch (e: Exception) {
                lastException = e
                Log.w("AIApiRepo", "AI 분석 요청 실패 (시도 $attempt/$MAX_RETRIES): ${e.message}")

                if (attempt == MAX_RETRIES) {
                    Log.e("AIApiRepo", "AI 분석 요청 최종 실패: ${e.message}")
                    break
                }
                delay(delayTime)
                delayTime *= 2
            }
        }

        val jsonResponseString = gptResponse?.choices?.firstOrNull()?.message?.content

        if (jsonResponseString != null) {
            val analysisResult = parseGptResponseToAIAnalysisResult(jsonResponseString)
            emit(analysisResult)
        } else if (lastException != null) {
            emit(createErrorAnalysisResult("AI 분석 응답을 가져오는 데 최종 실패했습니다. (오류: ${lastException.message})"))
        } else {
            emit(createErrorAnalysisResult("AI 분석 응답이 비어있습니다."))
        }
    }

    private fun createWorkoutSystemPrompt(): String {
        return """
        You are a rehabilitation workout planner AI.
        
        🚨 IMPORTANT INSTRUCTIONS:
        1. You MUST respond in **Korean** (한국어).
        2. You MUST respond in a valid JSON format.
        3. The 'scheduledDate' MUST strictly follow the format "M월 d일 (E)" (e.g., "11월 20일 (수)").
        
        JSON Structure:
        {
          "scheduledWorkouts": [
            {
              "scheduledDate": "String (Format: 'M월 d일 (E)', example: '11월 20일 (수)')",
              "exercises": [
                {
                  "name": "String (MUST match the name in AVAILABLE EXERCISES CATALOG)",
                  "description": "String (Detailed description based on user's injury)",
                  "bodyPart": "String",
                  "sets": "Int",
                  "reps": "Int",
                  "difficulty": "String (초급, 중급, 고급)",
                  "aiRecommendationReason": "String"
                }
              ]
            }
          ]
        }
        Ensure the response is ONLY the valid JSON object.
    """.trimIndent()
    }

    private fun createWorkoutUserPrompt(params: RecommendationParams): String {
        val todayDate = java.text.SimpleDateFormat("M월 d일 (E)", java.util.Locale.KOREA).format(java.util.Date())
        val exerciseCatalogJson = ExerciseCatalog.getExercisesJson()

        return """
            User Profile:
            Age: ${params.age}, Gender: ${params.gender}
            Height: ${params.heightCm} cm, Weight: ${params.weightKg} kg
            // [수정] 사용자 수준 판단을 위해 Activity Level과 Fitness Goal 추가
            Activity Level: ${params.activityLevel}
            Fitness Goal: ${params.fitnessGoal}
            
            Injury Area: ${params.injuryArea ?: "None"}
            Injury Type: ${params.injuryType ?: "N/A"}
            Injury Severity: ${params.injurySeverity ?: "N/A"}
            Additional Notes: ${params.additionalNotes ?: "None"}
            Past Sessions (for AI learning): ${gson.toJson(params.pastSessions)}

            🚨 [CRITICAL INSTRUCTION] 🚨
            Today is "$todayDate".
            
            AVAILABLE EXERCISES CATALOG (You MUST select the 'name' field ONLY from this list):
            $exerciseCatalogJson

            Rules:
            - The 'scheduledDate' of the FIRST item MUST BE "$todayDate".
            - The 'name' field **MUST EXACTLY** match an entry in the AVAILABLE EXERCISES CATALOG (Korean name).
            - Generate a **7-day workout plan** starting from "$todayDate".
            
            // [수정] 3~5개 운동 개수 및 난이도 조절 규칙 강화
            - **Constraint:** Each day MUST contain **3 to 5 exercises** (minimum 3, maximum 5).
            - **Personalization:** Select exercises and adjust difficulty/sets/reps based on the user's **Activity Level** and **Injury Severity**.
        """.trimIndent()
    }

    private fun createDietSystemPrompt(): String {
        return """
        You are a nutrition and diet planner AI for rehabilitation patients.
        
        🚨 IMPORTANT INSTRUCTIONS:
        1. You MUST respond in **Korean** (한국어).
        2. You MUST respond in a valid JSON format.
        3. The 'scheduledDate' MUST strictly follow the format "M월 d일 (E)" (e.g., "11월 20일 (수)").
        4. Keep 'aiRecommendationReason' VERY SHORT (maximum 10-15 characters in Korean).
        
        JSON Structure:
        {
          "scheduledDiets": [
            {
              "scheduledDate": "String (Format: 'M월 d일 (E)', example: '11월 20일 (수)')",
              "meals": [
                {
                  "mealType": "String (아침, 점심, 저녁, 간식)",
                  "foodItems": ["String"],
                  "ingredients": ["String"],
                  "calories": "Double",
                  "proteinGrams": "Double",
                  "carbs": "Double",
                  "fats": "Double",
                  "aiRecommendationReason": "String (MUST be very short, e.g., '단백질 보충', '에너지 공급')"
                }
              ]
            }
          ]
        }
        Ensure the response is ONLY the valid JSON object.
    """.trimIndent()
    }

    private fun createDietUserPrompt(params: RecommendationParams): String {
        val todayDate = java.text.SimpleDateFormat("M월 d일 (E)", java.util.Locale.KOREA).format(java.util.Date())

        return """
            User Profile:
            Age: ${params.age}, Gender: ${params.gender}
            Height: ${params.heightCm} cm, Weight: ${params.weightKg} kg
            Dietary Preferences: ${params.dietaryPreferences ?: "None"}
            Allergies: ${params.allergies ?: "None"}
            Injury Area: ${params.injuryArea ?: "None"}

            🚨 [CRITICAL INSTRUCTION] 🚨
            Today is "$todayDate".
            
            Rules:
            - The 'scheduledDate' of the FIRST item MUST BE "$todayDate".
            - Generate a **7-day diet plan** starting from "$todayDate".
            - Each day should have 3 meals (아침, 점심, 저녁 only - NO 간식).
            - You MUST provide a **different** menu for each day. Do NOT repeat the same meals.
            - Keep 'aiRecommendationReason' EXTREMELY SHORT (e.g., "단백질 보충", "에너지 공급", "회복 지원").
            - Consider the user's dietary preferences and allergies.
            - Focus on nutrition that supports rehabilitation and recovery.
        """.trimIndent()
    }

    // 운동 응답 파싱
    private fun parseWorkoutsResponse(jsonResponse: String): List<ScheduledWorkout> {
        try {
            data class WorkoutResponse(val scheduledWorkouts: List<ScheduledWorkout>)
            val response = gson.fromJson(jsonResponse, WorkoutResponse::class.java)
            return response.scheduledWorkouts
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("AIApiRepo", "운동 JSON 파싱 실패: ${e.message}")
            throw Exception("운동 데이터 파싱 실패: ${e.message}")
        }
    }

    // 식단 응답 파싱
    private fun parseDietsResponse(jsonResponse: String): List<ScheduledDiet> {
        try {
            data class DietResponse(val scheduledDiets: List<ScheduledDiet>)
            val response = gson.fromJson(jsonResponse, DietResponse::class.java)
            return response.scheduledDiets
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("AIApiRepo", "식단 JSON 파싱 실패: ${e.message}")
            throw Exception("식단 데이터 파싱 실패: ${e.message}")
        }
    }

    private fun createAnalysisSystemPrompt(): String {
        return """
            You are a professional rehabilitation analyst.
            Based on the user's profile and their past 7 days of rehab/diet sessions,
            provide concise, encouraging, and actionable feedback.
            Analyze the user's notes and ratings.
            
            🚨 IMPORTANT INSTRUCTION: You MUST respond entirely in Korean (한국어).
            
            🚨 You MUST respond in a valid JSON format that matches the AIAnalysisResult JSON structure:
            {
              "summary": "String",
              "strengths": ["String", "String"],
              "areasForImprovement": ["String", "String"],
              "personalizedTips": ["String"],
              "nextStepsRecommendation": "String",
              "disclaimer": "String"
            }
            Ensure the response is ONLY the valid JSON object.
        """.trimIndent()
    }

    private fun createAnalysisUserPrompt(rehabData: RehabData): String {
        val sessionsJson = gson.toJson(rehabData.pastRehabSessions)
        val dietSessionsJson = gson.toJson(rehabData.pastDietSessions)

        return """
            Here is the user's data for analysis:
            
            1. User Profile:
            ${gson.toJson(rehabData.userProfile)}

            2. Past 7 Days Rehab Sessions (note the 'userRating' 1-5 and 'notes'):
            $sessionsJson

            3. Past 7 Days Diet Sessions (note the 'userSatisfaction' 1-5 and 'notes'):
            $dietSessionsJson
            
            Please provide your analysis based on this data.
        """.trimIndent()
    }

    private fun parseGptResponseToAIAnalysisResult(gptResponse: String): AIAnalysisResult {
        try {
            return gson.fromJson(gptResponse, AIAnalysisResult::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            return createErrorAnalysisResult("GPT 분석 응답 JSON 파싱 실패: ${e.message}")
        }
    }

    private fun createErrorResult(message: String): AIRecommendationResult {
        return AIRecommendationResult(
            scheduledWorkouts = emptyList(),
            scheduledDiets = emptyList(),
            overallSummary = message,
            disclaimer = "오류가 발생했습니다."
        )
    }

    private fun createErrorAnalysisResult(message: String): AIAnalysisResult {
        return AIAnalysisResult(
            summary = message,
            strengths = emptyList(),
            areasForImprovement = emptyList(),
            personalizedTips = emptyList(),
            nextStepsRecommendation = "오류로 인해 분석을 완료할 수 없습니다.",
            disclaimer = "오류 발생"
        )
    }
}