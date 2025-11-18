package com.example.androidproject.data.repository

import com.example.androidproject.domain.model.AIAnalysisResult
import com.example.androidproject.domain.model.RehabData
import com.example.androidproject.domain.model.AIRecommendationResult
import com.example.androidproject.domain.model.RecommendationParams
import com.example.androidproject.domain.repository.AIApiRepository
import com.example.androidproject.data.network.GptApiService
import com.example.androidproject.data.network.dto.GptMessage
import com.example.androidproject.data.network.dto.GptRequest
import com.example.androidproject.data.network.dto.ResponseFormat
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AIApiRepositoryImpl @Inject constructor(
    private val gptApiService: GptApiService,
    private val gson: Gson
) : AIApiRepository {

    override suspend fun getAIRehabAndDietRecommendation(params: RecommendationParams): Flow<AIRecommendationResult> = flow {

        val systemPrompt = createGptSystemPrompt()
        val userPrompt = createGptUserPrompt(params)

        val request = GptRequest(
            model = "gpt-4-turbo", // 모델 확인 (권한 없으면 gpt-3.5-turbo로 변경)
            messages = listOf(
                GptMessage(role = "system", content = systemPrompt),
                GptMessage(role = "user", content = userPrompt)
            ),
            response_format = ResponseFormat(type = "json_object")
        )

        // [수정됨] API 호출 시 request만 전달 (API 키 파라미터 제거)
        val gptResponse = gptApiService.getChatCompletion(request = request)
        val jsonResponseString = gptResponse.choices.firstOrNull()?.message?.content

        if (jsonResponseString != null) {
            val aiResult = parseGptResponseToAIRecommendationResult(jsonResponseString)
            emit(aiResult)
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

        // [수정됨] API 호출 시 request만 전달
        val gptResponse = gptApiService.getChatCompletion(request = request)
        val jsonResponseString = gptResponse.choices.firstOrNull()?.message?.content

        if (jsonResponseString != null) {
            val analysisResult = parseGptResponseToAIAnalysisResult(jsonResponseString)
            emit(analysisResult)
        } else {
            emit(createErrorAnalysisResult("AI 분석 응답이 비어있습니다."))
        }

    }
    /**
     * (★수정★) AI 추천용 시스템 프롬프트
     * (HTTP 400 오류 해결을 위해 "JSON" 단어 추가)
     */
    private fun createGptSystemPrompt(): String {
        return """
            You are a long-term rehabilitation planner AI.
            Your goal is to create a systematic, multi-day workout plan (e.g., 5-7 days) that adapts to the user's progress.
            You MUST learn from the user's past session feedback (ratings and notes).
            
            🚨 You MUST respond in a valid JSON format that matches the AIRecommendationResult JSON structure. 
            Note the 'scheduledWorkouts' list.
            {
              "scheduledWorkouts": [
                {
                  "name": "String",
                  "description": "String",
                  "bodyPart": "String",
                  "sets": "Int",
                  "reps": "Int",
                  "difficulty": "String (초급, 중급, 고급)",
                  "aiRecommendationReason": "String",
                  "imageUrl": "String? (can be null)"
                }
              ],
              "recommendedDiets": [
                {
                  "mealType": "String (아침, 점심, 저녁, 간식)",
                  "foodItems": ["String", "String"],
                  "ingredients": ["String", "String"],
                  "calories": "Double?",
                  "proteinGrams": "Double?",
                  "carbs": "Double?",
                  "fats": "Double?",
                  "aiRecommendationReason": "String"
                }
              ],
              "overallSummary": "String?",
              "disclaimer": "String"
            }
            Ensure the response is ONLY the valid JSON object.
        """.trimIndent()
    }

    /**
     * (기존) 사용자 정보 전달 프롬프트
     */
    private fun createGptUserPrompt(params: RecommendationParams): String {
        val pastSessionsJson = gson.toJson(params.pastSessions)
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

            2. Past Performance (Learning Data - Note 'userRating' 1-5 and 'notes'):
            $pastSessionsJson

            Based on ALL this data, create a new multi-day workout plan.
            Remember to AVOID or MODIFY exercises with low ratings or negative feedback.
            If 'Past Performance' is empty or this is a new injury, create a new beginner plan.
        """.trimIndent()
    }

    /**
     * (기존) 추천 결과(JSON) 파싱
     */
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

    /**
     * (기존) 추천 오류 결과 생성
     */
    private fun createErrorResult(message: String): AIRecommendationResult {
        return AIRecommendationResult(
            scheduledWorkouts = emptyList(),
            recommendedDiets = emptyList(),
            overallSummary = message,
            disclaimer = "오류가 발생했습니다."
        )
    }

    /**
     * (★수정★) AI 분석용 시스템 프롬프트
     * (HTTP 400 오류 해결을 위해 "JSON" 단어 추가)
     */
    private fun createAnalysisSystemPrompt(): String {
        return """
            You are a professional rehabilitation analyst.
            Based on the user's profile and their past 7 days of rehab/diet sessions,
            provide concise, encouraging, and actionable feedback.
            Analyze the user's notes and ratings.
            
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

    /**
     * (기존) AI 분석용 사용자 데이터 프롬프트
     */
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

    /**
     * (기존) AI 분석 응답(JSON) 파싱
     */
    private fun parseGptResponseToAIAnalysisResult(gptResponse: String): AIAnalysisResult {
        try {
            return gson.fromJson(gptResponse, AIAnalysisResult::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            return createErrorAnalysisResult("GPT 분석 응답 JSON 파싱 실패: ${e.message}")
        }
    }

    /**
     * (기존) AI 분석 오류 결과 생성
     */
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