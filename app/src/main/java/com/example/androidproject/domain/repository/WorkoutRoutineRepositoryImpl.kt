package com.example.androidproject.data.repository

import com.example.androidproject.data.local.datasource.LocalDataSource
import com.example.androidproject.data.local.entity.ScheduledWorkoutEntity
import com.example.androidproject.domain.model.AIRecommendationResult
import com.example.androidproject.domain.model.ExerciseRecommendation // 👈 [추가] import
import com.example.androidproject.domain.model.Injury
import com.example.androidproject.domain.model.RecommendationParams
import com.example.androidproject.domain.model.ScheduledWorkout
import com.example.androidproject.domain.model.User
import com.example.androidproject.domain.repository.AIApiRepository // 👈 [추가] import
import com.example.androidproject.domain.repository.RehabSessionRepository // 👈 [추가] import
import com.example.androidproject.domain.repository.WorkoutRoutineRepository
import com.google.gson.Gson // 👈 [추가] import
import com.google.gson.reflect.TypeToken // 👈 [추가] import
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class WorkoutRoutineRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDataSource,
    private val aiApiRepository: AIApiRepository,
    private val rehabSessionRepository: RehabSessionRepository
) : WorkoutRoutineRepository {

    /**
     * 🚨 [수정] 'override'만 남기고 'suspend'는 '삭제'된 상태인지 확인
     */
    override fun getWorkoutRoutine(
        forceReload: Boolean,
        user: User,
        injury: Injury?
    ): Flow<AIRecommendationResult> = flow {

        val userId = user.id

        if (forceReload) {
            localDataSource.clearWorkouts(userId)
        }

        val dbCache = localDataSource.getWorkouts(userId).first()

        if (dbCache.isNotEmpty() && !forceReload) {
            emit(dbCache.toDomainResult())
            return@flow
        }

        // AI 학습을 위해 '실제' 과거 기록 조회
        val pastSessions = rehabSessionRepository.getRehabHistory(userId).first()

        val recommendationParams = RecommendationParams(
            userId = user.id,
            age = user.age,
            gender = user.gender,
            heightCm = user.heightCm,
            weightKg = user.weightKg,
            activityLevel = user.activityLevel,
            fitnessGoal = user.fitnessGoal,
            dietaryPreferences = user.preferredDietaryTypes,
            allergies = user.allergyInfo,
            equipmentAvailable = user.equipmentAvailable,
            currentPainLevel = user.currentPainLevel,
            injuryArea = injury?.bodyPart,
            injuryType = injury?.name,
            injurySeverity = injury?.severity,
            additionalNotes = user.additionalNotes,
            pastSessions = pastSessions
        )

        aiApiRepository.getAIRehabAndDietRecommendation(recommendationParams)
            .collect { aiResult ->
                localDataSource.upsertWorkouts(aiResult.toEntity(userId))
                emit(localDataSource.getWorkouts(userId).first().toDomainResult())
            }
    }

    // --- Mapper 함수들 ---

    private fun AIRecommendationResult.toEntity(userId: String): List<ScheduledWorkoutEntity> {
        return this.scheduledWorkouts.map {
            ScheduledWorkoutEntity(
                userId = userId,
                scheduledDate = it.scheduledDate,
                exercisesJson = Gson().toJson(it.exercises)
            )
        }
    }

    private fun List<ScheduledWorkoutEntity>.toDomainResult(): AIRecommendationResult {
        val gson = Gson()
        val workouts = this.map {
            ScheduledWorkout(
                scheduledDate = it.scheduledDate,
                exercises = gson.fromJson(
                    it.exercisesJson,
                    object : TypeToken<List<ExerciseRecommendation>>() {}.type
                )
            )
        }
        return AIRecommendationResult(
            scheduledWorkouts = workouts,
            recommendedDiets = emptyList(), // (식단은 이 Repository가 관리하지 않음)
            overallSummary = workouts.firstOrNull()?.exercises?.firstOrNull()?.aiRecommendationReason
        )
    }
}