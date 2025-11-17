package com.example.androidproject.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * AI API의 'Exercise'(운동) JSON 응답과 1:1로 매칭되는 데이터 '그릇'입니다.
 * (상세 로드맵 Phase 1)
 *
 * Domain 모델과 비슷하지만, 이 DTO는 오직 API 응답을 받기 위해서만 존재합니다.
 */
data class ExerciseDto(
    // @SerializedName은 API의 JSON 키 이름과
    // DTO의 변수 이름이 다를 때 매칭시켜줍니다.

    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String,

    // API에서 "body_part"라는 이름으로 JSON 데이터가 온다면
    // @SerializedName("body_part")를 사용합니다.
    @SerializedName("bodyPart")
    val bodyPart: String,

    @SerializedName("difficulty")
    val difficulty: String,

    @SerializedName("videoUrl")
    val videoUrl: String?, // API에서 null이 올 수 있음

    @SerializedName("precautions")
    val precautions: String?,

    @SerializedName("aiRecommendationReason")
    val aiRecommendationReason: String?
)