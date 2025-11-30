// seungc1/androidproject/androidProject-dev/app/src/main/java/com/example/androidproject/data/local/dao/DietSessionDao.kt

package com.dataDoctor.rehabai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dataDoctor.rehabai.data.local.entity.DietSessionEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface DietSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addDietSession(session: DietSessionEntity) : Long

    @Query("SELECT * FROM diet_session_table WHERE userId = :userId ORDER BY dateTime DESC")
    fun getDietHistory(userId: String): Flow<List<DietSessionEntity>>

    // [수정] BETWEEN 대신 >= :startDate AND dateTime < :endDate 를 사용
    @Query("SELECT * FROM diet_session_table WHERE userId = :userId AND dateTime >= :startDate AND dateTime < :endDate")
    fun getSessionsBetween(userId: String, startDate: Date, endDate: Date): Flow<List<DietSessionEntity>>
}