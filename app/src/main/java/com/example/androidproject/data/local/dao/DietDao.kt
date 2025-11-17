package com.example.androidproject.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.androidproject.data.local.entity.DietEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DietDao {
    /**
     * AI가 추천한 식단 목록을 '식단 사전'에 저장합니다.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDiets(diets: List<DietEntity>)

    /**
     * ID로 특정 식단 정보를 가져옵니다. (상세보기, 식단 기록 시 사용)
     */
    @Query("SELECT * FROM diet_table WHERE id = :dietId LIMIT 1")
    fun getDietById(dietId: String): Flow<DietEntity?>
}