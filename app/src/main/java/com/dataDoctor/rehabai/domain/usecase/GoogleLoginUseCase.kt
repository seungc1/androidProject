package com.dataDoctor.rehabai.domain.usecase

import com.dataDoctor.rehabai.domain.repository.UserRepository
import javax.inject.Inject

/**
 * [새 파일] 구글 로그인을 처리하는 Use Case
 * Presentation Layer(ViewModel)에서 호출하면 Repository에 로그인을 요청합니다.
 */
class GoogleLoginUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(idToken: String): String? {
        return userRepository.loginWithGoogle(idToken)
    }
}