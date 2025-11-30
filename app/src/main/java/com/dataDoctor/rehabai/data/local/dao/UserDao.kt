package com.dataDoctor.rehabai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dataDoctor.rehabai.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * '아이디' '중복' '확인' '쿼리' '추가'
 */
@Dao
interface UserDao {
    // (기존) '삽입'/'업데이트'
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUser(user: UserEntity)

    // 'ID'로 '사용자' '조회'
    @Query("SELECT * FROM UserEntity WHERE id = :userId LIMIT 1")
    fun getUserById(userId: String) : Flow<UserEntity?>

    // 'ID' '중복' '확인' '쿼리' ('회원가입'용)
    @Query("SELECT COUNT(*) FROM UserEntity WHERE id = :id")
    suspend fun getUserCountById(id: String): Int
}