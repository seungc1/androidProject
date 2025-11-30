package com.dataDoctor.rehabai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * AI가 생성한 '멀티-데이 루틴'을 저장하기 위한 Room Entity.
 * 이 Entity는 특정 '사용자(userId)'에 종속됩니다.
 */
@Entity(tableName = "scheduled_workout_table")
data class ScheduledWorkoutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: String, // 어떤 사용자의 루틴인지 식별
    val scheduledDate: String, // AI가 지정한 날짜 (예: "11월 17일 (월)")

    /**
     * 해당 날짜의 운동 목록 (List<ExerciseRecommendation>)을
     * Gson을 사용해 JSON 문자열로 변환하여 저장합니다.
     */
    val exercisesJson: String
)