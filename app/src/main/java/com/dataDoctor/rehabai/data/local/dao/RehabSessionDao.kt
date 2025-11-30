// seungc1/androidproject/androidProject-dev/app/src/main/java/com/example/androidproject/data/local/dao/RehabSessionDao.kt

package com.dataDoctor.rehabai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dataDoctor.rehabai.data.local.entity.RehabSessionEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface RehabSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addRehabSession(session: RehabSessionEntity)

    @Query("SELECT * FROM rehab_session_table WHERE userId = :userId ORDER BY dateTime DESC")
    fun getRehabHistory(userId: String): Flow<List<RehabSessionEntity>>

    // [수정] BETWEEN 대신 >= :startDate AND dateTime < :endDate 를 사용
    @Query("SELECT * FROM rehab_session_table WHERE userId = :userId AND dateTime >= :startDate AND dateTime < :endDate")
    fun getSessionsBetween(userId: String, startDate: Date, endDate: Date): Flow<List<RehabSessionEntity>>
}