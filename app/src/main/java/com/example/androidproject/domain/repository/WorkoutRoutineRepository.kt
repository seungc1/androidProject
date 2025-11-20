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
     * ★★★ [추가] 운동 완료 상태가 변경되었을 때 전체 루틴을 저장소에 다시 저장(Upsert)합니다. ★★★
     */
    suspend fun upsertWorkoutRoutineState(userId: String, updatedFullRoutine: List<ScheduledWorkout>)
}