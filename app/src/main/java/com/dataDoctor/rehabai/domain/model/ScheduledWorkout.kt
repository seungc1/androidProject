package com.dataDoctor.rehabai.domain.model

/**
 * AI가 생성한 '특정 날짜'의 운동 루틴.
 */
data class ScheduledWorkout(
    val scheduledDate: String, // AI가 지정한 날짜 (예: "2025-11-17 월요일")
    val exercises: List<ExerciseRecommendation> // 해당 날짜에 수행할 운동 목록
)