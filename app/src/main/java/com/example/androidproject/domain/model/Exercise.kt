// app/src/main/java/com/example/androidproject/domain/model/Exercise.kt
package com.example.androidproject.domain.model

// 재활 운동 정보를 나타내는 데이터 클래스
data class Exercise(
    val id: String,
    val name: String,
    val description: String,
    val bodyPart: String, // 운동 대상 부위
    val difficulty: String, // 예: "초급", "중급", "고급"
    val videoUrl: String?, // 운동 영상 URL (선택 사항)
    val precautions: String?, // 주의사항 (선택 사항)
    val aiRecommendationReason: String? = null // AI가 이 운동을 추천한 이유 (선택 사항)
)