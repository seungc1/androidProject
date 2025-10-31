package com.example.androidproject.domain.model

/**
 * AI가 추천하는 개별 식단 항목에 대한 상세 정보.
 * Domain Layer Model
 */
data class Diet(
    /** 식단을 식별하는 고유 ID. */
    val id: String,

    /** 식사의 종류 (예: "아침", "점심", "저녁", "간식"). */
    val mealType: String,

    /** 식단 또는 음식의 이름 (예: "닭가슴살 샐러드", "오트밀"). */
    val foodName: String,

    /** 권장 섭취량. */
    val quantity: Double,

    /** 섭취량의 단위 (예: "g", "ml", "개"). */
    val unit: String,

    /** 해당 식단의 총 칼로리. */
    val calorie: Int,

    /** 단백질 함량 (g). */
    val protein: Double,

    /** 지방 함량 (g). */
    val fat: Double,

    /** 탄수화물 함량 (g). */
    val carbs: Double,

    /** 주요 재료 목록. 알레르기 필터링에 활용. */
    val ingredients: List<String>,

    /** 조리 방법 또는 간단한 팁 (선택 사항). */
    val preparationTips: String?,

    /** AI가 이 식단을 추천한 구체적인 이유 (선택 사항). */
    val aiRecommendationReason: String?
)
