package com.example.androidproject.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "diet_session_table")
data class DietSessionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val dietId: String,
    val dateTime: Date, // TypeConverter가 Long으로 변환해 줌
    val actualQuantity: Double,
    val actualUnit: String,
    val userSatisfaction: Int?,
    val notes: String?,
    val foodName: String?, // [추가] 사용자 입력 음식 이름
    val photoUrl: String? // [추가] 사진 경로
)