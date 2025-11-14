package com.example.androidproject.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * AI API의 'Diet'(식단) JSON 응답과 1:1로 매칭되는 DTO입니다.
 * (상세 로드맵 Phase 1)
 */
data class DietDto(
    @SerializedName("id")
    val id: String,

    // API에서 "meal_type"으로 온다면 @SerializedName("meal_type")
    @SerializedName("mealType")
    val mealType: String,

    @SerializedName("foodName")
    val foodName: String,

    @SerializedName("quantity")
    val quantity: Double,

    @SerializedName("unit")
    val unit: String,

    @SerializedName("calorie")
    val calorie: Int,

    @SerializedName("protein")
    val protein: Double,

    @SerializedName("fat")
    val fat: Double,

    @SerializedName("carbs")
    val carbs: Double,

    // API에서 List<String>을 지원하는지 확인해야 합니다.
    @SerializedName("ingredients")
    val ingredients: List<String>,

    @SerializedName("preparationTips")
    val preparationTips: String?,

    @SerializedName("aiRecommendationReason")
    val aiRecommendationReason: String?
)