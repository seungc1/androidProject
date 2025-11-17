package com.example.androidproject.domain.repository

import com.example.androidproject.domain.model.AIRecommendationResult
import com.example.androidproject.domain.model.Injury
import com.example.androidproject.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * AI 루틴 '캐싱(Caching)' 로직을 담당할 Repository 인터페이스
 */
interface WorkoutRoutineRepository {
    suspend fun getWorkoutRoutine(
        forceReload: Boolean,
        user: User,
        injury: Injury?
    ): Flow<AIRecommendationResult>
}