package com.example.androidproject.domain.model

/**
 * AI (GPT) 모델로부터 받은 재활 운동 및 식단 추천 결과.
 * (★수정★) ExerciseRecommendation, DietRecommendation 클래스를 이 파일에서 "삭제".
 */
data class AIRecommendationResult(
    val scheduledWorkouts: List<ScheduledWorkout>, // GPT가 생성한 '멀티-데이' 운동 계획
    val scheduledDiets: List<ScheduledDiet>, // 날짜별 식단 리스트로 변경
    val overallSummary: String? = null,
    val disclaimer: String = "본 추천은 AI에 의해 생성되었으며, 전문 의료인의 진단 및 조언을 대체할 수 없습니다."
)
