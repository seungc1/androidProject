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
            model = "gpt-4-turbo",
            messages = listOf(
                GptMessage(role = "system", content = systemPrompt),
                GptMessage(role = "user", content = userPrompt)
            ),
            response_format = ResponseFormat(type = "json_object")
        )

        val gptResponse = gptApiService.getChatCompletion(request = request)
        val jsonResponseString = gptResponse.choices.firstOrNull()?.message?.content

        if (jsonResponseString != null) {
            val aiResult = parseGptResponseToAIRecommendationResult(jsonResponseString)
            emit(aiResult)
        } else {
            // (AI 응답이 비어있는 경우, 성공이지만 빈 목록으로 간주)
            emit(createErrorResult("AI 응답이 비어있습니다."))
        }
    }

    /**
     * (★수정★) 'try-catch'를 '제거'합니다.
     */
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

        val gptResponse = gptApiService.getChatCompletion(request = request)
        val jsonResponseString = gptResponse.choices.firstOrNull()?.message?.content

        if (jsonResponseString != null) {
            val analysisResult = parseGptResponseToAIAnalysisResult(jsonResponseString)
            emit(analysisResult)
        } else {
            emit(createErrorAnalysisResult("AI 분석 응답이 비어있습니다."))
        }

    }

    private fun createGptSystemPrompt(): String {
        return """
            You are a long-term rehabilitation planner AI.
            ... (중략) ...
            {
              "scheduledWorkouts": [ ... ],
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
              ...
            }
            Ensure the response is ONLY the valid JSON object.
        """.trimIndent()
    }

    private fun createGptUserPrompt(params: RecommendationParams): String {
        val pastSessionsJson = gson.toJson(params.pastSessions)
        return """
            Here is the user's information and past performance:
            ... (중략) ...
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
            recommendedDiets = emptyList(),
            overallSummary = message,
            disclaimer = "오류가 발생했습니다."
        )
    }

    private fun createAnalysisSystemPrompt(): String {
        return """
            You are a professional rehabilitation analyst.
            ... (중략) ...
        """.trimIndent()
    }

    private fun createAnalysisUserPrompt(rehabData: RehabData): String {
        val sessionsJson = gson.toJson(rehabData.pastRehabSessions)
        val dietSessionsJson = gson.toJson(rehabData.pastDietSessions)
        return """
            Here is the user's data for analysis:
            ... (중략) ...
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