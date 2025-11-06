package com.example.androidproject.domain.usecase

import com.example.androidproject.domain.model.DietSession
import com.example.androidproject.domain.repository.DietSessionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 새로운 식단 섭취 기록 세션을 추가하는 Use Case.
 * Presentation Layer에서 사용자의 식단 섭취 데이터를 받아 Repository로 전달합니다.
 *
 * @param dietSessionRepository 식단 섭취 기록 데이터를 관리하는 Repository
 */
class AddDietSessionUseCase @Inject constructor(
    private val dietSessionRepository: DietSessionRepository
) {
    /**
     * 새로운 식단 섭취 기록을 비동기적으로 추가합니다.
     *
     * @param session 추가할 DietSession 객체
     * @return 작업 성공 여부를 나타내는 Flow<Unit>
     */
    suspend operator fun invoke(session: DietSession): Flow<Unit> {
        // Repository에 기록을 추가하는 역할을 위임합니다.
        return dietSessionRepository.addDietSession(session)
    }
}