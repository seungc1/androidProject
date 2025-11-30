package com.dataDoctor.rehabai.domain.usecase

import com.dataDoctor.rehabai.domain.model.User
import com.dataDoctor.rehabai.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 사용자 프로필 정보를 업데이트하는 Use Case.
 * 단순히 UserRepository의 함수를 호출하는 위임 역할을 수행합니다.
 *
 * @param userRepository 사용자 데이터를 관리하는 Repository
 */
class UpdateUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    /**
     * 특정 사용자의 프로필 정보를 비동기적으로 업데이트합니다.
     *
     * @param user 업데이트할 User 객체
     * @return 작업 성공 여부를 나타내는 Flow<Unit>
     */
    suspend operator fun invoke(user: User): Flow<Unit> {
        return userRepository.updateUserProfile(user)
    }
}