// app/src/main/java/com/example/androidproject/domain/model/AIRecommendationResult.kt

package com.example.androidproject.domain.model

/**
 * AI (GPT) 모델로부터 받은 재활 운동 및 식단 추천 결과.
 * Data Layer에서 GPT API의 텍스트 응답을 이 구조로 파싱합니다.
 */
data class AIRecommendationResult(
    val recommendedExercises: List<ExerciseRecommendation>, // GPT가 추천한 운동 목록
    val recommendedDiets: List<DietRecommendation>,        // GPT가 추천한 식단 목록
    val overallSummary: String? = null, // GPT가 제공하는 전체적인 요약 텍스트
    val disclaimer: String = "본 추천은 AI에 의해 생성되었으며, 전문 의료인의 진단 및 조언을 대체할 수 없습니다."
)

data class ExerciseRecommendation(
    val name: String,             // 운동 이름
    val description: String,      // 운동 설명 (GPT가 생성)
    val bodyPart: String,         // 관련 신체 부위 (GPT가 생성)
    val sets: Int,                // 세트 수
    val reps: Int,                // 반복 횟수
    val difficulty: String,       // 난이도 (예: "초급", "중급", "고급")
    val aiRecommendationReason: String? = null, // GPT가 추천한 이유
    val imageUrl: String? = null  // (선택 사항) 운동 이미지 URL
)

data class DietRecommendation(
    val mealType: String,         // 식사 종류 (예: "아침", "점심", "저녁", "간식")
    val foodItems: List<String>,  // 식단 구성 요소 리스트 (예: "닭가슴살", "현미밥", "샐러드")
    val ingredients: List<String>,// 식단에 포함된 주요 재료 (알레르기 필터링용)
    val calories: Double? = null, // 예상 칼로리
    val proteinGrams: Double? = null, // 단백질
    val carbs: Double? = null,    // 탄수화물
    val fats: Double? = null,     // 지방
    val aiRecommendationReason: String? = null // GPT가 추천한 이유
)