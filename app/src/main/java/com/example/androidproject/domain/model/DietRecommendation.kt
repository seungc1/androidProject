package com.example.androidproject.domain.model

/**
 * AI (GPT) 모델이 추천한 '개별 식단' 정보.
 * (AIRecommendationResult.kt에서 분리됨)
 */
data class DietRecommendation(
    val mealType: String,
    val foodItems: List<String>,
    val ingredients: List<String>,
    val calories: Double? = null,
    val proteinGrams: Double? = null,
    val carbs: Double? = null,
    val fats: Double? = null,
    val aiRecommendationReason: String? = null
)