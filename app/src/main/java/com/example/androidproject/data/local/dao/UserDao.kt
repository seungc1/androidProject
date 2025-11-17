package com.example.androidproject.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.androidproject.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    // ---삽입(Insert) ---
    // 사용자 정보를 삽입(추가)합니다.
    // OnConflictStrategy.REPLACE : 만약 이미 같은 id를 가진 사용자가 있다면, 덮어쓰기(REPLACE) 합니다. ex) 업데이트
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUser(user: UserEntity)

    @Query("SELECT * FROM UserEntity WHERE id = :userId LIMIT 1")
    fun getUserById(userId: String) : Flow<UserEntity?>

}