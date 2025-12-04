package com.dataDoctor.rehabai.data.repository

import com.dataDoctor.rehabai.data.ExerciseCatalog
import com.dataDoctor.rehabai.data.network.model.*
import com.dataDoctor.rehabai.domain.model.AIAnalysisResult
import com.dataDoctor.rehabai.domain.model.RehabData
import com.dataDoctor.rehabai.domain.model.AIRecommendationResult
import com.dataDoctor.rehabai.domain.model.RecommendationParams
import com.dataDoctor.rehabai.domain.model.ScheduledWorkout
import com.dataDoctor.rehabai.domain.model.ScheduledDiet
import com.dataDoctor.rehabai.domain.repository.AIApiRepository
import com.dataDoctor.rehabai.data.network.GptApiService
import com.google.gson.Gson
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
        // ★★★ 병렬 처리: 운동과 식단을 동시에 요청하여 시간 단축 및 타임아웃 방지 ★★★

        coroutineScope {
            // 1. 운동 계획 요청 (비동기 시작)
            val workoutDeferred = async {
                try {
                    fetchWorkouts(params)
                } catch (e: Exception) {
                    Log.e("AIApiRepo", "운동 추천 실패: ${e.message}")
                    emptyList<ScheduledWorkout>() // 실패 시 빈 리스트 반환
                }
            }

            // 2. 식단 계획 요청 (비동기 시작)
            val dietDeferred = async {
                try {
                    fetchDiets(params)
                } catch (e: Exception) {
                    Log.e("AIApiRepo", "식단 추천 실패: ${e.message}")
                    emptyList<ScheduledDiet>() // 실패 시 빈 리스트 반환
                }
            }

            // 3. 두 작업이 끝날 때까지 대기
            val workoutsResult = workoutDeferred.await()
            val dietsResult = dietDeferred.await()

            Log.d("AIApiRepo", "최종 결과: 운동 ${workoutsResult.size}개, 식단 ${dietsResult.size}개")

            // 4. 결과 반환 (둘 다 비어있으면 에러 메시지 전달)
            if (workoutsResult.isEmpty() && dietsResult.isEmpty()) {
                emit(createErrorResult("AI가 데이터를 생성하지 못했습니다. 잠시 후 다시 시도해주세요."))
            } else {
                emit(AIRecommendationResult(
                    scheduledWorkouts = workoutsResult,
                    scheduledDiets = dietsResult,
                    overallSummary = "AI 맞춤 재활 및 식단 계획이 생성되었습니다.",
                    disclaimer = "본 추천은 AI에 의해 생성되었으며, 전문 의료인의 진단 및 조언을 대체할 수 없습니다."
                ))
            }
        }
    }

    // ★★★ 운동 계획 요청 (gpt-4-turbo) ★★★
    private suspend fun fetchWorkouts(params: RecommendationParams): List<ScheduledWorkout> {
        val systemPrompt = createWorkoutSystemPrompt()
        val userPrompt = createWorkoutUserPrompt(params)

        val request = GptRequest(
            model = "gpt-4-turbo", // [수정] 모델 변경
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

        for (attempt in 1..MAX_RETRIES) {
            try {
                gptResponse = gptApiService.getChatCompletion(request = request)
                Log.d("AIApiRepo", "운동 API 요청 성공 (시도 $attempt)")
                break
            } catch (e: Exception) {
                Log.w("AIApiRepo", "운동 API 요청 실패 (시도 $attempt/$MAX_RETRIES): ${e.message}")
                if (attempt == MAX_RETRIES) throw e
                delay(delayTime)
                delayTime *= 2
            }
        }

        val jsonResponseString = gptResponse?.choices?.firstOrNull()?.message?.content
            ?: throw Exception("운동 API 응답이 비어있습니다.")

        Log.d("AIApiRepo", "운동 Raw JSON Response: $jsonResponseString")
        val cleanJson = jsonResponseString.replace(Regex("^```json\\s*|\\s*```$"), "").trim()

        return parseWorkoutsResponse(cleanJson)
    }

    // ★★★ 식단 계획 요청 (gpt-4-turbo) ★★★
    private suspend fun fetchDiets(params: RecommendationParams): List<ScheduledDiet> {
        val systemPrompt = createDietSystemPrompt()
        val userPrompt = createDietUserPrompt(params)

        val request = GptRequest(
            model = "gpt-4-turbo", // [수정] 모델 변경
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

        for (attempt in 1..MAX_RETRIES) {
            try {
                gptResponse = gptApiService.getChatCompletion(request = request)
                Log.d("AIApiRepo", "식단 API 요청 성공 (시도 $attempt)")
                break
            } catch (e: Exception) {
                Log.w("AIApiRepo", "식단 API 요청 실패 (시도 $attempt/$MAX_RETRIES): ${e.message}")
                if (attempt == MAX_RETRIES) throw e
                delay(delayTime)
                delayTime *= 2
            }
        }

        val jsonResponseString = gptResponse?.choices?.firstOrNull()?.message?.content
            ?: throw Exception("식단 API 응답이 비어있습니다.")

        Log.d("AIApiRepo", "식단 Raw JSON Response: $jsonResponseString")
        val cleanJson = jsonResponseString.replace(Regex("^```json\\s*|\\s*```$"), "").trim()

        return parseDietsResponse(cleanJson)
    }

    // ★★★ 주간 분석 요청 (gpt-4-turbo) ★★★
    override suspend fun analyzeRehabProgress(rehabData: RehabData): Flow<AIAnalysisResult> = flow {
        val systemPrompt = createAnalysisSystemPrompt()
        val userPrompt = createAnalysisUserPrompt(rehabData)

        val request = GptRequest(
            model = "gpt-4-turbo", // [수정] 모델 변경
            messages = listOf(
                GptMessage(role = "system", content = systemPrompt),
                GptMessage(role = "user", content = userPrompt)
            ),
            response_format = ResponseFormat(type = "json_object")
        )

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
                if (attempt == MAX_RETRIES) break
                delay(delayTime)
                delayTime *= 2
            }
        }

        val jsonResponseString = gptResponse?.choices?.firstOrNull()?.message?.content

        if (jsonResponseString != null) {
            val analysisResult = parseGptResponseToAIAnalysisResult(jsonResponseString)
            emit(analysisResult)
        } else {
            emit(createErrorAnalysisResult("AI 분석 응답을 가져오는 데 실패했습니다. (${lastException?.message})"))
        }
    }

    // =========================================================
    // ★★★ 헬퍼 함수들 (프롬프트 및 파싱) ★★★
    // =========================================================

    private fun createWorkoutSystemPrompt(): String {
        return """
        You are a rehabilitation workout planner AI.
        
        🚨 IMPORTANT INSTRUCTIONS:
        1. You MUST respond in **Korean** (한국어).
        2. You MUST respond in a valid JSON format.
        3. The 'scheduledDate' MUST strictly follow the format "M월 d일 (E)" (e.g., "12월 4일 (목)").
        
        JSON Structure:
        {
          "scheduledWorkouts": [
            {
              "scheduledDate": "String (Format: 'M월 d일 (E)', example: '12월 4일 (목)')",
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
            Injury Area: ${params.injuryArea ?: "None"}
            Injury Type: ${params.injuryType ?: "N/A"}
            Injury Severity: ${params.injurySeverity ?: "N/A"}
            Additional Notes: ${params.additionalNotes ?: "None"}
            Past Sessions: ${gson.toJson(params.pastSessions)}

            🚨 [CRITICAL INSTRUCTION] 🚨
            Today is "$todayDate".
            
            AVAILABLE EXERCISES CATALOG (You MUST select the 'name' field ONLY from this list):
            $exerciseCatalogJson

            Rules:
            - The 'scheduledDate' of the FIRST item MUST BE "$todayDate".
            - The 'name' field **MUST EXACTLY** match an entry in the AVAILABLE EXERCISES CATALOG (Korean name).
            - Generate a **7-day workout plan** starting from "$todayDate".
            - Each day MUST contain a minimum of 3 exercises and a maximum of 5, appropriate for the user's injury.
        """.trimIndent()
    }

    private fun createDietSystemPrompt(): String {
        return """
        You are a nutrition and diet planner AI for rehabilitation patients.
        
        🚨 IMPORTANT INSTRUCTIONS:
        1. You MUST respond in **Korean** (한국어).
        2. You MUST respond in a valid JSON format.
        3. The 'scheduledDate' MUST strictly follow the format "M월 d일 (E)" (e.g., "12월 4일 (목)").
        
        JSON Structure:
        {
          "scheduledDiets": [
            {
              "scheduledDate": "String (Format: 'M월 d일 (E)', example: '12월 4일 (목)')",
              "meals": [
                {
                  "mealType": "String (아침, 점심, 저녁, 간식)",
                  "foodItems": ["String"],
                  "ingredients": ["String"],
                  "calories": "Double",
                  "proteinGrams": "Double",
                  "carbs": "Double",
                  "fats": "Double",
                  "aiRecommendationReason": "String"
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
            - Each day should have 3 meals (아침, 점심, 저녁).
            - You MUST provide a **different** menu for each day.
            - Consider the user's dietary preferences and allergies.
        """.trimIndent()
    }

    // 운동 응답 파싱
    private fun parseWorkoutsResponse(jsonResponse: String): List<ScheduledWorkout> {
        try {
            data class WorkoutResponse(val scheduledWorkouts: List<ScheduledWorkout>)
            val response = gson.fromJson(jsonResponse, WorkoutResponse::class.java)
            return response.scheduledWorkouts
        } catch (e: Exception) {
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
            Log.e("AIApiRepo", "식단 JSON 파싱 실패: ${e.message}")
            throw Exception("식단 데이터 파싱 실패: ${e.message}")
        }
    }

    private fun createAnalysisSystemPrompt(): String {
        return """
            You are a professional rehabilitation analyst.
            Respond in Korean, JSON format matching AIAnalysisResult.
            JSON Structure:
            {
              "summary": "String",
              "strengths": ["String"],
              "areasForImprovement": ["String"],
              "personalizedTips": ["String"],
              "nextStepsRecommendation": "String",
              "disclaimer": "String"
            }
        """.trimIndent()
    }

    private fun createAnalysisUserPrompt(rehabData: RehabData): String {
        return "User Data: ${gson.toJson(rehabData)}"
    }

    private fun parseGptResponseToAIAnalysisResult(gptResponse: String): AIAnalysisResult {
        try {
            return gson.fromJson(gptResponse, AIAnalysisResult::class.java)
        } catch (e: Exception) {
            return createErrorAnalysisResult("파싱 실패: ${e.message}")
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