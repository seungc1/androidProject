package com.example.androidproject.data.repository

import com.example.androidproject.data.ExerciseCatalog
import com.example.androidproject.domain.model.AIAnalysisResult
import com.example.androidproject.domain.model.RehabData
import com.example.androidproject.domain.model.AIRecommendationResult
import com.example.androidproject.domain.model.RecommendationParams
import com.example.androidproject.domain.repository.AIApiRepository
import com.example.androidproject.data.network.GptApiService
import com.example.androidproject.data.network.model.GptMessage
import com.example.androidproject.data.network.model.GptRequest
import com.example.androidproject.data.network.model.GptResponse
import com.example.androidproject.data.network.model.ResponseFormat
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

        val systemPrompt = createGptSystemPrompt()
        val userPrompt = createGptUserPrompt(params)

        val request = GptRequest(
            model = "gpt-4-turbo", // 모델 유지
            messages = listOf(
                GptMessage(role = "system", content = systemPrompt),
                GptMessage(role = "user", content = userPrompt)
            ),
            response_format = ResponseFormat(type = "json_object")
        )

        // ★★★ 429 오류 해결을 위한 재시도 로직 시작 ★★★
        val MAX_RETRIES = 5
        var delayTime = 1000L // 1초부터 시작
        var gptResponse: GptResponse? = null
        var lastException: Exception? = null

        for (attempt in 1..MAX_RETRIES) {
            try {
                // 실제 API 호출
                gptResponse = gptApiService.getChatCompletion(request = request)
                Log.d("AIApiRepo", "AI API 요청 성공 (시도 $attempt)")
                break
            } catch (e: Exception) {
                lastException = e
                Log.w("AIApiRepo", "AI API 요청 실패 (시도 $attempt/$MAX_RETRIES): ${e.message}")

                if (attempt == MAX_RETRIES) {
                    Log.e("AIApiRepo", "AI API 요청 최종 실패: ${e.message}")
                    break
                }

                // 지수 백오프: 다음 시도 전까지 대기 시간을 두 배로 늘립니다.
                delay(delayTime)
                delayTime *= 2
            }
        }
        // ★★★ 429 오류 해결을 위한 재시도 로직 종료 ★★★

        // gptResponse의 필드에 접근 (choices, message, content)
        val jsonResponseString = gptResponse?.choices?.firstOrNull()?.message?.content

        if (jsonResponseString != null) {
            val aiResult = parseGptResponseToAIRecommendationResult(jsonResponseString)
            emit(aiResult)
        } else if (lastException != null) {
            // 재시도 후에도 최종적으로 실패한 경우 오류 반환
            emit(createErrorResult("AI 응답을 가져오는 데 최종 실패했습니다. (오류: ${lastException.message})"))
        } else {
            emit(createErrorResult("AI 응답이 비어있습니다."))
        }
    }

    override suspend fun analyzeRehabProgress(rehabData: RehabData): Flow<AIAnalysisResult> = flow {
        val systemPrompt = createAnalysisSystemPrompt()
        val userPrompt = createAnalysisUserPrompt(rehabData)

        val request = GptRequest(
            model = "gpt-4-turbo",
            messages = listOf(
                GptMessage(role = "system", content = systemPrompt),
                GptMessage(role = "user", content = userPrompt)
            ),
            response_format = ResponseFormat(type = "json_object")
        )

        // ★★★ 429 오류 해결을 위한 재시도 로직 시작 (analyzeProgress) ★★★
        val MAX_RETRIES = 5
        var delayTime = 1000L
        var gptResponse: GptResponse? = null
        var lastException: Exception? = null

        for (attempt in 1..MAX_RETRIES) {
            try {
                // 실제 API 호출
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

                // 지수 백오프: 다음 시도 전까지 대기 시간을 두 배로 늘립니다.
                delay(delayTime)
                delayTime *= 2
            }
        }
        // ★★★ 429 오류 해결을 위한 재시도 로직 종료 (analyzeProgress) ★★★

        val jsonResponseString = gptResponse?.choices?.firstOrNull()?.message?.content

        if (jsonResponseString != null) {
            val analysisResult = parseGptResponseToAIAnalysisResult(jsonResponseString)
            emit(analysisResult)
        } else if (lastException != null) {
            // 재시도 후에도 최종적으로 실패한 경우 오류 반환
            emit(createErrorAnalysisResult("AI 분석 응답을 가져오는 데 최종 실패했습니다. (오류: ${lastException.message})"))
        } else {
            emit(createErrorAnalysisResult("AI 분석 응답이 비어있습니다."))
        }
    }

    // =========================================================
    // ★★★ 모든 헬퍼 함수는 클래스 내부로 이동됨 (오류 해결) ★★★
    // =========================================================

    private fun createGptSystemPrompt(): String {
        return """
        You are a long-term rehabilitation planner AI.
        Your goal is to create a systematic, multi-day workout plan (e.g., 5-7 days) that adapts to the user's progress.
        
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
                  "description": "String (New detailed description based on user's injury/notes)",
                  "bodyPart": "String",
                  "sets": "Int",
                  "reps": "Int",
                  "difficulty": "String (초급, 중급, 고급)",
                  "aiRecommendationReason": "String"
                  // imageUrl 필드는 앱에서 로컬로 처리하므로 제거되었습니다.
                }
              ]
            }
          ],
          "scheduledDiets": [
            {
              "scheduledDate": "String (Format: 'M월 d일 (E)', same as workouts)",
              "meals": [
                {
                  "mealType": "String (아침, 점심, 저녁, 간식)",
                  "foodItems": ["String"],
                  "ingredients": ["String"],
                  "calories": "Double?",
                  "proteinGrams": "Double?",
                  "carbs": "Double?",
                  "fats": "Double?",
                  "aiRecommendationReason": "String"
                }
              ]
            }
          ],
          "overallSummary": "String",
          "disclaimer": "String"
        }
        Ensure the response is ONLY the valid JSON object.
    """.trimIndent()
    }

    private fun createGptUserPrompt(params: RecommendationParams): String {
        val pastSessionsJson = gson.toJson(params.pastSessions)

        // (중요) 오늘 날짜 구하기 (앱과 동일한 포맷 사용)
        val todayDate = java.text.SimpleDateFormat("M월 d일 (E)", java.util.Locale.KOREA).format(java.util.Date())

        // ★★★ 운동 카탈로그 JSON 가져오기 ★★★
        val exerciseCatalogJson = ExerciseCatalog.getExercisesJson()

        return """
            Here is the user's information and past performance:
            
            1. User Profile (Static):
            User ID: ${params.userId}
            Age: ${params.age}
            Gender: ${params.gender}
            Height: ${params.heightCm} cm
            Weight: ${params.weightKg} kg
            Injury Area: ${params.injuryArea ?: "None"}
            Injury Type: ${params.injuryType ?: "N/A"}
            Injury Severity: ${params.injurySeverity ?: "N/A"}
            Additional Notes: ${params.additionalNotes ?: "None"}

            2. Past Performance (Learning Data):
            $pastSessionsJson

            🚨 [CRITICAL INSTRUCTION] 🚨
            Today is "$todayDate".
            
            3. AVAILABLE EXERCISES CATALOG (You MUST select the 'name' field ONLY from this list):
            $exerciseCatalogJson

            You MUST strictly adhere to the following rules for generating 'scheduledWorkouts':
            - The 'scheduledDate' of the FIRST item in the array MUST BE "$todayDate".
            - The 'name' field in your JSON output **MUST EXACTLY** match an entry in the 'AVAILABLE EXERCISES CATALOG' (Korean name).
            - The 'description', 'sets', 'reps', and 'aiRecommendationReason' fields must be newly generated based on the user's profile and injury condition.

            🚨 [DIET INSTRUCTION] 🚨
            - You MUST provide a **different** diet menu for each day. Do NOT repeat the same meal plan for multiple days.
            - You MUST generate a plan for **7 days** (both workouts and diets).
            - Consider the user's dietary preferences and allergies.

            Based on ALL this data, create a new **7-day workout and diet plan** starting from "$todayDate".
        """.trimIndent()
    }

    private fun parseGptResponseToAIRecommendationResult(gptResponse: String): AIRecommendationResult {
        try {
            val result = gson.fromJson(gptResponse, AIRecommendationResult::class.java)
            return result.copy(
                scheduledWorkouts = result.scheduledWorkouts ?: emptyList(),
                disclaimer = result.disclaimer.ifEmpty { "본 추천은 AI에 의해 생성되었으며, 전문 의료인의 진단 및 조언을 대체할 수 없습니다." }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return createErrorResult("GPT 응답 JSON 파싱 실패: ${e.message}")
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