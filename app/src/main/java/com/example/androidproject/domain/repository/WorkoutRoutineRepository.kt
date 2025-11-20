// 파일 경로: app/src/main/java/com/example/androidproject/domain/repository/WorkoutRoutineRepository.kt
package com.example.androidproject.domain.repository

import com.example.androidproject.domain.model.AIRecommendationResult
import com.example.androidproject.domain.model.Injury
import com.example.androidproject.domain.model.ScheduledWorkout
import com.example.androidproject.domain.model.User
import kotlinx.coroutines.flow.Flow

interface WorkoutRoutineRepository {

    fun getWorkoutRoutine(
        forceReload: Boolean,
        user: User,
        injury: Injury?
    ): Flow<AIRecommendationResult>

    /**
     * ★★★ [추가] 운동 완료 상태 영속성 함수 ★★★
     */
    suspend fun upsertWorkoutRoutineState(userId: String, updatedFullRoutine: List<ScheduledWorkout>)
}