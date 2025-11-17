package com.example.androidproject.domain.usecase

import com.example.androidproject.domain.repository.UserRepository
import javax.inject.Inject

/**
 * (★ 수정 ★) '로그인' '성공' '시' 'userId'를 '반환'합니다.
 */
class LoginUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    // (★ 수정 ★) '반환' '타입' 'Boolean' -> 'String?' (userId 또는 null)
    suspend operator fun invoke(id: String, password: String): String? {
        // (★ 수정 ★) 'UserRepository'의 'login' '함수'가 'userId' '또는' 'null'을 '반환'
        return userRepository.login(id, password)
    }
}