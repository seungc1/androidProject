package com.dataDoctor.rehabai.domain.model

// AIRecommendationResult.kt 파일에 정의된 클래스들을 Import하여 사용합니다.


/**
 * AI (GPT) 모델로부터 받은 재활 운동 및 식단 추천 결과의 요약 정보.
 * 이 클래스는 전체 추천 세션의 제목과 요약을 담습니다.
 */
data class AIRecommendation(
    val title: String, // 추천 세션의 전체적인 제목
    val summary: String, // 추천에 대한 간략한 요약 (GPT가 생성)
    // AIRecommendationResult.kt에 정의된 리스트를 사용합니다.
    val recommendedExercises: List<ExerciseRecommendation>,
    val recommendedDiet: List<DietRecommendation>,
    val disclaimer: String = "본 추천은 AI에 의해 생성되었으며, 전문 의료인의 진단 및 조언을 대체할 수 없습니다."
)

