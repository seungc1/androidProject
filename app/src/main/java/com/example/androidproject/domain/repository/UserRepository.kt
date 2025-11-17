package com.example.androidproject.domain.repository

import com.example.androidproject.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    // 특정 사용자의 프로필 정보 가져오기
    suspend fun getUserProfile(userId: String): Flow<User>

    // 사용자 프로필 정보 업데이트
    suspend fun updateUserProfile(user: User): Flow<Unit> // Unit은 성공/실패 여부만 반환할 때 사용

    // 로그인 시도 (성공 여부 반환)
    suspend fun login(id: String, password: String): Boolean
}