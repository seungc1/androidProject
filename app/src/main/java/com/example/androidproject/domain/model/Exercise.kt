package com.example.androidproject.domain.model

/**
 * 개별 운동에 대한 상세 정보.
 * Domain Layer Model
 */
data class Exercise(
    /** 운동을 식별하는 고유 ID. */
    val id: String,

    /** 운동의 이름 (예: "스쿼트"). */
    val name: String,

    /** 운동 방법 및 효과에 대한 상세 설명. */
    val description: String,

    /** 운동 대상 신체 부위 (예: "하체", "코어"). */
    val bodyPart: String,

    /** 운동 난이도 (예: "초급", "중급", "고급"). */
    val difficulty: String,

    /** 운동 동작을 보여주는 영상의 URL (선택 사항). */
    val videoUrl: String?,

    /** 운동 시 주의해야 할 점 (선택 사항). */
    val precautions: String?,

    /** AI가 이 운동을 추천한 구체적인 이유 (선택 사항). */
    val aiRecommendationReason: String?
)
