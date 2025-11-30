package com.dataDoctor.rehabai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scheduled_diet_table")
data class ScheduledDietEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val scheduledDate: String, // 예: "11월 19일 (수)"
    val dietsJson: String // List<DietRecommendation>을 JSON으로 변환해 저장
)