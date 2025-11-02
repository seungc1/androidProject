package com.example.androidproject.domain.model

data class Diet(
    val id: String,
    val mealType: String, // 예: "아침", "점심", "저녁", "간식"
    val foodName: String,
    val quantity: Double, // 양 (예: 100.0)
    val unit: String, // 단위 (예: "g", "ml", "개")
    val calorie: Int,
    val protein: Double, // 단백질 (g)
    val fat: Double, // 지방 (g)
    val carbs: Double, // 탄수화물 (g)
    val ingredients: List<String>, // 주요 재료 목록 (예: ["닭가슴살", "현미", "브로콜리"])
    val preparationTips: String?, // 조리 팁 (선택 사항)
    val aiRecommendationReason: String? = null // AI가 이 식단을 추천한 이유 (선택 사항)
)