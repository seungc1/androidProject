package com.dataDoctor.rehabai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dataDoctor.rehabai.data.local.entity.InjuryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InjuryDao {
    /**
     * 부상 정보를 삽입하거나 업데이트합니다.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertInjury(injury: InjuryEntity)

    /**
     * ID로 특정 부상 정보를 가져옵니다.
     */
    @Query("SELECT * FROM injury_table WHERE id = :injuryId LIMIT 1")
    fun getInjuryById(injuryId: String): Flow<InjuryEntity?>

    /**
     * 특정 사용자의 모든 부상 목록을 가져옵니다. (이력 관리용)
     */
    @Query("SELECT * FROM injury_table WHERE userId = :userId")
    fun getInjuriesForUser(userId: String): Flow<List<InjuryEntity>>
}