package com.example.androidproject.domain.usecase

import com.example.androidproject.domain.repository.UserRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    // 로그인 시도 (성공/실패 여부 반환)
    suspend operator fun invoke(id: String, password: String): Boolean {
        return userRepository.login(id, password)
    }
}