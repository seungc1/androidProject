package com.example.androidproject.data.repository

import com.example.androidproject.domain.model.AIRecommendationResult
import com.example.androidproject.domain.model.RecommendationParams
import com.example.androidproject.domain.model.ExerciseRecommendation // ⭐ Exercise가 아닌 ExerciseRecommendation
import com.example.androidproject.domain.model.DietRecommendation // ⭐ Diet가 아닌 DietRecommendation
import com.example.androidproject.domain.repository.AIApiRepository // ⭐ 1. import 문 추가 ⭐

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

// AIApiRepository 인터페이스를 구현합니다.
class AIApiRepositoryImpl @Inject constructor(
    // private val gptApiService: GptApiService
) : AIApiRepository { // AIApiRepository를 구현한다고 명시

    override suspend fun getAIRehabAndDietRecommendation(params: RecommendationParams): Flow<AIRecommendationResult> = flow {

        val prompt = createGptPrompt(params)

        // ... (실제 API 호출 로직은 나중에) ...

        // ⭐ 2. 더미 데이터를 ExerciseRecommendation, DietRecommendation 모델로 생성 ⭐
        val dummyExercises = listOf(
            ExerciseRecommendation( // ⭐ Exercise -> ExerciseRecommendation
                name = "팔굽혀펴기 변형",
                description = "${params.gender}님, ${params.injuryArea} 부상을 고려한 팔굽혀펴기입니다.",
                bodyPart = "가슴, 어깨", sets = 3, reps = 10, difficulty = "중급",
                aiRecommendationReason = "부상 부위 외 주변 근육 강화에 도움을 줍니다.",
                imageUrl = null // imageUrl 필드 추가
            )
        )
        val dummyDiets = listOf(
            DietRecommendation( // ⭐ Diet -> DietRecommendation
                mealType = "점심",
                foodItems = listOf("닭가슴살 샐러드", "고구마 100g"),
                ingredients = listOf("닭가슴살", "양상추", "토마토", "고구마"),
                calories = 400.0, proteinGrams = 30.0, carbs = 40.0, fats = 15.0,
                aiRecommendationReason = "${params.fitnessGoal}에 필요한 고단백, 저탄수 식단입니다."
            )
        )
        val aiResult = AIRecommendationResult(
            recommendedExercises = dummyExercises,
            recommendedDiets = dummyDiets,
            overallSummary = "${params.gender}님의 ${params.injuryArea} 부상과 ${params.fitnessGoal}을 위한 맞춤형 추천입니다.",
            disclaimer = "더미 데이터로 생성된 추천입니다."
        )

        emit(aiResult) // Flow로 결과 발행
    }

    // ... (createGptPrompt, parseGptResponseToAIRecommendationResult 함수는 그대로) ...
    private fun createGptPrompt(params: RecommendationParams): String {
        return """
            You are a rehabilitation and diet recommendation AI assistant.
            Based on the following user information, provide personalized rehabilitation exercises and diet plans in JSON format.
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

            Provide a JSON response with two lists: "recommendedExercises" and "recommendedDiets".
            Each exercise should include: name, description, bodyPart, sets, reps, difficulty (초급, 중급, 고급), aiRecommendationReason, and imageUrl (can be null).
            Each diet item should include: mealType (아침, 점심, 저녁, 간식), foodItems (list of strings), ingredients (list of strings for filtering), calories (Double), proteinGrams (Double), carbs (Double), fats (Double), and aiRecommendationReason.
            Also include an overallSummary for the recommendation.
            Ensure the response is a valid JSON.
        """.trimIndent()
    }

    private fun parseGptResponseToAIRecommendationResult(gptResponse: String): AIRecommendationResult {
        return AIRecommendationResult(
            recommendedExercises = emptyList(),
            recommendedDiets = emptyList(),
            overallSummary = "GPT 응답 파싱 실패 또는 더미 응답입니다."
        )
    }
}