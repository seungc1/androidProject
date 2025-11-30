package com.dataDoctor.rehabai.domain.repository

import com.dataDoctor.rehabai.domain.model.AIRecommendationResult
import com.dataDoctor.rehabai.domain.model.Injury
import com.dataDoctor.rehabai.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * AI 루틴 '캐싱(Caching)' 로직을 담당할 Repository 인터페이스
 */
interface WorkoutRoutineRepository {
    /**
     * 🚨 [수정] 'suspend' 키워드를 '삭제'합니다.
     * (반환 타입이 Flow이므로 suspend 함수가 아닙니다.)
     */
    fun getWorkoutRoutine(
        forceReload: Boolean,
        user: User,
        injury: Injury?
    ): Flow<AIRecommendationResult>
}