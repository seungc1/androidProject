package com.example.androidproject.domain.repository

import com.example.androidproject.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * [수정 파일 6/8] 'UserRepository' '인터페이스'
 * 'login' '함수' '반환' '타입' '변경'
 * 'checkUserExists', 'createUser' '함수' '추가'
 */
interface UserRepository {
    // (기존) '프로필' '조회'
    suspend fun getUserProfile(userId: String): Flow<User>

    // (기존) '프로필' '업데이트'
    suspend fun updateUserProfile(user: User): Flow<Unit>

    // (기존) '로그인' '시도' (성공 시 'userId', '실패' 시 'null' '반환')
    suspend fun login(id: String, password: String): String?

    // (★ 추가 ★) 구글 로그인
    suspend fun loginWithGoogle(idToken: String): String?

    // (기존) '아이디' '중복' '확인' ('회원가입'용)
    suspend fun checkUserExists(id: String): Boolean

    // (기존) '새' '사용자' '생성' ('회원가입'용)
    suspend fun createUser(user: User): Flow<Unit>
}