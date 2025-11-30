package com.dataDoctor.rehabai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dataDoctor.rehabai.data.local.entity.ExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    // --- 삽입 (Insert) ---
    // AI 추천 결과(운동 목록)를 한번에 삽입(upsert)합니다.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertExercises(exercises: List<ExerciseEntity>)

    // --- 조회 (Select) ---
    // 특정 신체 부위(bodyPart)에 해당하는 운동 목록을 가져옵니다.
    // Flow로 반환하여 데이터 변경 시 자동으로 UI가 업데이트되도록 합니다.
    @Query("SELECT * FROM execise_table WHERE bodyPart = :bodyPart")
    fun getExercisesByBodyPart(bodyPart: String): Flow<List<ExerciseEntity>>

    // ID로 특정 운동 1개를 조회합니다.
    @Query("SELECT * FROM execise_table WHERE id = :execiseId LIMIT 1")
    fun getExerciseById(execiseId: String): Flow<ExerciseEntity?>

    // (선택 사항) 모든 운동 목록을 조회합니다.
    @Query("SELECT * FROM execise_table")
    fun getAllExercises(): Flow<List<ExerciseEntity>>
}