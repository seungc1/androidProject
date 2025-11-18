package com.example.androidproject.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.androidproject.data.local.entity.RehabSessionEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface RehabSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addRehabSession(session: RehabSessionEntity)

    @Query("SELECT * FROM rehab_session_table WHERE userId = :userId ORDER BY dateTime DESC")
    fun getRehabHistory(userId: String): Flow<List<RehabSessionEntity>>

    // (★ 수정 ★) 'BETWEEN' 대신 명확한 부등호 사용 (start <= time < end)
    // 이렇게 해야 '다음날 00:00:00'이 포함되지 않아 날짜가 겹치지 않습니다.
    @Query("SELECT * FROM rehab_session_table WHERE userId = :userId AND dateTime >= :startDate AND dateTime < :endDate")
    fun getSessionsBetween(userId: String, startDate: Date, endDate: Date): Flow<List<RehabSessionEntity>>
}