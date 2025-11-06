package com.example.androidproject.domain.usecase

import com.example.androidproject.domain.model.RehabSession
import com.example.androidproject.domain.repository.RehabSessionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 새로운 재활 운동 기록 세션을 추가하는 Use Case.
 * Presentation Layer에서 사용자의 운동 완료 데이터를 받아 Repository로 전달합니다.
 *
 * @param rehabSessionRepository 재활 운동 기록 데이터를 관리하는 Repository
 */
class AddRehabSessionUseCase @Inject constructor(
    private val rehabSessionRepository: RehabSessionRepository
) {
    /**
     * 새로운 재활 운동 기록을 비동기적으로 추가합니다.
     *
     * @param session 추가할 RehabSession 객체
     * @return 작업 성공 여부를 나타내는 Flow<Unit>
     */
    suspend operator fun invoke(session: RehabSession): Flow<Unit> {
        // Repository에 기록을 추가하는 역할을 위임합니다.
        return rehabSessionRepository.addRehabSession(session)
    }
}