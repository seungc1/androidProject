package com.dataDoctor.rehabai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "rehab_session_table")
data class RehabSessionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val exerciseId: String,
    val dateTime: Date, // TypeConverter가 Long으로 변환해 줌
    val sets: Int,
    val reps: Int,
    val durationMinutes: Int?,
    val userRating: Int?,
    val notes: String?
)