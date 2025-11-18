package com.example.androidproject.domain.usecase

import com.example.androidproject.domain.model.User
import com.example.androidproject.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * [새 파일 2/4]
 * '새' '사용자' '정보'를 '받아' '회원가입'을 '처리'하는 Use Case
 */
class SignupUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(user: User): Flow<Unit> {
        // 'UserRepository'에 '사용자' '생성'을 '요청'
        return userRepository.createUser(user)
    }
}