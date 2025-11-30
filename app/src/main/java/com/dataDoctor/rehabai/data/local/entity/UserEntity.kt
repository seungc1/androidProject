package com.dataDoctor.rehabai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserEntity(
    @PrimaryKey val id: String,
    val password: String,
    val name: String,
    val gender : String,
    val age: Int,
    val heightCm: Int,
    val weightKg: Double,
    val activityLevel: String,
    val fitnessGoal: String,
    val allergyInfo: String,
    val preferredDietType: String,
    val targetCalories: Int?,
    val currentInjuryId: String?,
    val preferredDietaryTypes: String,
    val equipmentAvailable: String,
    val currentPainLevel: Int,
    val additionalNotes: String?
)