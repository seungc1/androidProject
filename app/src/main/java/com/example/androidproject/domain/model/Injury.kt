package com.example.androidproject.domain.model

/**
 * 사용자가 겪고 있는 부상에 대한 상세 정보.
 * Domain Layer Model
 */
data class Injury(
    /** 부상을 식별하는 고유 ID. */
    val id: String,

    /** 부상의 명칭 (예: "손목 염좌"). */
    val name: String,

    /** 부상이 있는 신체 부위 (예: "손목", "어깨"). */
    val bodyPart: String,

    /** 부상의 심각도 (예: "경미", "보통", "심각"). */
    val severity: String,

    /** 부상에 대한 상세 설명. */
    val description: String
)
