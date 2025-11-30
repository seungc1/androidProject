package com.dataDoctor.rehabai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "execise_table")
data class ExerciseEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val bodyPart: String,
    val difficulty: String,
    val precautions: String?,
    val aiRecommendationReason: String?,
    val imageName: String?
)