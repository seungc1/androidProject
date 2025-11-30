package com.dataDoctor.rehabai.domain.usecase

import com.dataDoctor.rehabai.domain.repository.UserRepository
import javax.inject.Inject

/**
 * [새 파일 1/4]
 * '회원가입' 시 '아이디' '중복'을 '확인'하는 Use Case
 */
class CheckUserExistsUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(id: String): Boolean {
        // 'UserRepository'에 'checkUserExists' '함수'를 '요청'
        return userRepository.checkUserExists(id)
    }
}