package com.dataDoctor.rehabai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * [부상 정보]를 저장하기 위한 Room Entity
 */
@Entity(tableName = "injury_table")
data class InjuryEntity(
    @PrimaryKey val id: String,
    val userId: String, // 어떤 사용자의 부상 정보인지
    val name: String, // 예: "손목 염좌" (질환명)
    val bodyPart: String, // 예: "손목" (환부)
    val severity: String, // 예: "경미", "보통", "심각"
    val description: String // 상세 설명
)