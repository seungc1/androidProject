package com.example.androidproject.domain.model

/**
 * AI (GPT) 모델이 추천한 '개별 운동' 정보.
 * (AIRecommendationResult.kt에서 분리됨)
 */
data class ExerciseRecommendation(
    val name: String,
    val description: String,
    val bodyPart: String,
    val sets: Int,
    val reps: Int,
    val difficulty: String,
    val aiRecommendationReason: String? = null,
    val imageUrl: String? = null
)