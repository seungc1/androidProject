package com.dataDoctor.rehabai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dataDoctor.rehabai.data.local.entity.ScheduledWorkoutEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduledWorkoutDao {

    /**
     * AI가 생성한 새 루틴을 통째로 삽입합니다.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWorkouts(workouts: List<ScheduledWorkoutEntity>)

    /**
     * 특정 사용자의 모든 루틴 계획을 가져옵니다.
     */
    @Query("SELECT * FROM scheduled_workout_table WHERE userId = :userId ORDER BY id ASC")
    fun getWorkouts(userId: String): Flow<List<ScheduledWorkoutEntity>>

    /**
     * 특정 사용자의 기존 루틴을 모두 삭제합니다.
     * (AI가 새 루틴을 생성하기 전에 호출)
     */
    @Query("DELETE FROM scheduled_workout_table WHERE userId = :userId")
    suspend fun clearWorkouts(userId: String)
}