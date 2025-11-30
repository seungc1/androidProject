package com.dataDoctor.rehabai.domain.model

// 재활 운동 정보를 나타내는 데이터 클래스
data class Exercise(
    val id: String,
    val name: String,
    val description: String,
    val bodyPart: String,
    val difficulty: String,
    val precautions: String? = null,
    val sets: Int? = null,
    val reps: Int? = null,
    val aiRecommendationReason: String? = null,
    val imageName: String? = null
)