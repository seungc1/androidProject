package com.example.androidproject.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.androidproject.data.local.entity.DietSessionEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface DietSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addDietSession(session: DietSessionEntity)

    @Query("SELECT * FROM diet_session_table WHERE userId = :userId ORDER BY dateTime DESC")
    fun getDietHistory(userId: String): Flow<List<DietSessionEntity>>

    // UseCase에서 7일간의 기록을 요청하므로
    @Query("SELECT * FROM diet_session_table WHERE userId = :userId AND dateTime BETWEEN :startDate AND :endDate")
    fun getSessionsBetween(userId: String, startDate: Date, endDate: Date): Flow<List<DietSessionEntity>>
}