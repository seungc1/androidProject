package com.dataDoctor.rehabai.domain.model

/**
 * AI가 생성한 '특정 날짜'의 식단 루틴.
 */
data class ScheduledDiet(
    val scheduledDate: String, // 예: "11월 19일 (수)"
    val meals: List<DietRecommendation> // 아침, 점심, 저녁 식단 목록
)