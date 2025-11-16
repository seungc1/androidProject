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

// AIApiRepository 인터페이스를 구현합니다.
class AIApiRepositoryImpl @Inject constructor(
    private val gptApiService: GptApiService,
    private val gson: Gson
) : AIApiRepository { // AIApiRepository를 구현한다고 명시

    /**
     * AI 추천 요청
     */
    override suspend fun getAIRehabAndDietRecommendation(params: RecommendationParams): Flow<AIRecommendationResult> = flow {

        // 1. GPT 프롬프트를 '시스템' 역할과 '사용자' 역할로 분리
        val systemPrompt = createGptSystemPrompt()
        val userPrompt = createGptUserPrompt(params)

        // 2. GPT API 요청 객체 생성
        val request = GptRequest(
            model = "gpt-4-turbo", // (사용할 모델 - JSON 모드 지원 모델)
            messages = listOf(
                GptMessage(role = "system", content = systemPrompt),
                GptMessage(role = "user", content = userPrompt)
            ),
            response_format = ResponseFormat(type = "json_object") // JSON 응답 요청
        )

        try {
            // 3. 실제 API 호출
            val gptResponse = gptApiService.getChatCompletion(request = request)

            // GPT 응답 (JSON 문자열)
            val jsonResponseString = gptResponse.choices.firstOrNull()?.message?.content

            if (jsonResponseString != null) {
                // 4. GPT가 반환한 JSON 문자열을 AIRecommendationResult 객체로 파싱
                val aiResult = parseGptResponseToAIRecommendationResult(jsonResponseString)
                emit(aiResult)
            } else {
                // 응답이 비어있는 경우
                emit(createErrorResult("AI 응답이 비어있습니다."))
            }

        } catch (e: Exception) {
            // 5. 네트워크 또는 API 오류 처리
            e.printStackTrace() // (실제 앱에서는 로깅)
            emit(createErrorResult("AI 추천을 가져오는 데 실패했습니다: ${e.message}"))
        }
    }

    /**
     *  AI 주간 분석 리포트 요청
     */
    override suspend fun analyzeRehabProgress(rehabData: RehabData): Flow<AIAnalysisResult> = flow {
        val systemPrompt = createAnalysisSystemPrompt()
        val userPrompt = createAnalysisUserPrompt(rehabData)

        val request = GptRequest(
            model = "gpt-4-turbo", // (분석용 모델)
            messages = listOf(
                GptMessage(role = "system", content = systemPrompt),
                GptMessage(role = "user", content = userPrompt)
            ),
            response_format = ResponseFormat(type = "json_object") // AIAnalysisResult JSON 요청
        )

        try {
            val gptResponse = gptApiService.getChatCompletion(request = request)
            val jsonResponseString = gptResponse.choices.firstOrNull()?.message?.content

            if (jsonResponseString != null) {
                // (★핵심★) AI 응답 JSON 문자열을 AIAnalysisResult 객체로 파싱
                val analysisResult = parseGptResponseToAIAnalysisResult(jsonResponseString)
                emit(analysisResult)
            } else {
                emit(createErrorAnalysisResult("AI 분석 응답이 비어있습니다."))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emit(createErrorAnalysisResult("AI 분석을 가져오는 데 실패했습니다: ${e.message}"))
        }
    }
    /**
     * AI의 역할을 정의하는 시스템 프롬프트
     */
    private fun createGptSystemPrompt(): String {
        return """
            You are a rehabilitation and diet recommendation AI assistant.
            Based on the user information, provide personalized rehabilitation exercises and diet plans.
            You MUST respond in a valid JSON format that matches the following structure:
            {
              "recommendedExercises": [
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
            Ensure the response is ONLY the valid JSON object, without any surrounding text or markdown.
        """.trimIndent()
    }

    /**
     * 사용자 정보 전달 프롬프트
     */
    private fun createGptUserPrompt(params: RecommendationParams): String {
        return """
            Here is the user's information:
            User ID: ${params.userId}
            Age: ${params.age}
            Gender: ${params.gender}
            Height: ${params.heightCm} cm
            Weight: ${params.weightKg} kg
            Activity Level: ${params.activityLevel}
            Fitness Goal: ${params.fitnessGoal}
            Dietary Preferences: ${params.dietaryPreferences.joinToString()}
            Allergies: ${params.allergies.joinToString()}
            Equipment Available: ${params.equipmentAvailable.joinToString()}
            Current Pain Level (1-10): ${params.currentPainLevel}
            Injury Area: ${params.injuryArea ?: "None"}
            Injury Type: ${params.injuryType ?: "N/A"}
            Injury Severity: ${params.injurySeverity ?: "N/A"}
            Additional Notes: ${params.additionalNotes ?: "None"}
        """.trimIndent()
    }

    /**
     * 추천 결과(JSON) 파싱
     */
    private fun parseGptResponseToAIRecommendationResult(gptResponse: String): AIRecommendationResult {
        try {
            // Gson을 사용하여 JSON 문자열을 AIRecommendationResult 객체로 변환
            val result = gson.fromJson(gptResponse, AIRecommendationResult::class.java)

            // AI가 disclaimer를 제공하지 않았을 경우를 대비해 기본값 설정
            return result.copy(
                disclaimer = result.disclaimer.ifEmpty { "본 추천은 AI에 의해 생성되었으며, 전문 의료인의 진단 및 조언을 대체할 수 없습니다." }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            // JSON 파싱 실패 시
            return createErrorResult("GPT 응답 JSON 파싱 실패: ${e.message}")
        }
    }

    /**
     * 추천 오류 결과 생성
     */
    private fun createErrorResult(message: String): AIRecommendationResult {
        return AIRecommendationResult(
            recommendedExercises = emptyList(),
            recommendedDiets = emptyList(),
            overallSummary = message,
            disclaimer = "오류가 발생했습니다."
        )
    }

    /**
     * AI 분석(AIAnalysisResult)을 위한 시스템 프롬프트
     */
    private fun createAnalysisSystemPrompt(): String {
        return """
            You are a professional rehabilitation analyst.
            Based on the user's profile and their past 7 days of rehab/diet sessions,
            provide concise, encouraging, and actionable feedback.
            Analyze the user's notes and ratings.
            You MUST respond in a valid JSON format that matches the AIAnalysisResult structure:
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
     * AI 분석을 위한 사용자 데이터 프롬프트
     */
    private fun createAnalysisUserPrompt(rehabData: RehabData): String {
        // (세션 데이터를 JSON으로 변환하여 프롬프트에 포함)
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
     * AI 분석 응답(JSON)을 AIAnalysisResult 객체로 파싱
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
     * AI 분석 오류 시 반환할 객체
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