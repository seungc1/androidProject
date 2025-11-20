// 파일 경로: app/src/main/java/com/example/androidproject/domain/model/ExerciseRecommendation.kt
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
    val imageUrl: String? = null,
    // ★★★ [추가] 운동 완료 상태 필드 ★★★
    val isCompleted: Boolean = false
)