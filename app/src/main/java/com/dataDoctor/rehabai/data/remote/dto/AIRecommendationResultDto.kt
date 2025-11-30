package com.dataDoctor.rehabai.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * AI API의 최종 추천 결과(운동 + 식단) JSON 응답과
 * 1:1로 매칭되는 DTO입니다.
 * (상세 로드맵 Phase 1)
 *
 * 이 DTO는 내부에 ExerciseDto 리스트와 DietDto 리스트를 포함합니다.
 */
data class AIRecommendationResultDto(

    // API JSON 키가 "recommended_exercises"일 경우를 가정합니다.
    @SerializedName("recommendedExercises")
    val recommendedExercises: List<ExerciseDto>,

    // API JSON 키가 "recommended_diets"일 경우를 가정합니다.
    @SerializedName("recommendedDiets")
    val recommendedDiets: List<DietDto>
)