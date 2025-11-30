package com.dataDoctor.rehabai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * [식단 사전] (AI가 추천했거나 사용자가 추가한)을 저장하기 위한 Room Entity
 */
@Entity(tableName = "diet_table")
data class DietEntity(
    @PrimaryKey val id: String,
    val mealType: String,
    val foodName: String,
    val quantity: Double,
    val unit: String,
    val calorie: Int,
    val protein: Double,
    val fat: Double,
    val carbs: Double,
    val ingredients: String, // List<String>을 쉼표로 구분된 String으로 저장
    val preparationTips: String?,
    val aiRecommendationReason: String?
)