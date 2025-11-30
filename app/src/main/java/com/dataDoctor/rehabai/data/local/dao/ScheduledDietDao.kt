package com.dataDoctor.rehabai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dataDoctor.rehabai.data.local.entity.ScheduledDietEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduledDietDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDiets(diets: List<ScheduledDietEntity>)

    @Query("SELECT * FROM scheduled_diet_table WHERE userId = :userId")
    fun getScheduledDiets(userId: String): Flow<List<ScheduledDietEntity>>

    @Query("DELETE FROM scheduled_diet_table WHERE userId = :userId")
    suspend fun clearScheduledDiets(userId: String)
}