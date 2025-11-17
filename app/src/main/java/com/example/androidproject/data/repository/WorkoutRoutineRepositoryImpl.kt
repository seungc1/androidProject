package com.example.androidproject.data.repository

import com.example.androidproject.data.local.datasource.LocalDataSource
import com.example.androidproject.data.local.entity.ScheduledWorkoutEntity
import com.example.androidproject.domain.model.*
import com.example.androidproject.domain.repository.AIApiRepository
import com.example.androidproject.domain.repository.RehabSessionRepository
import com.example.androidproject.domain.repository.WorkoutRoutineRepository // 👈 (Interface import)
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

// 🚨 [해결책] ': WorkoutRoutineRepository' 부분이 빠졌는지 확인하세요.
class WorkoutRoutineRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDataSource,
    private val aiApiRepository: AIApiRepository,
    private val rehabSessionRepository: RehabSessionRepository
) : WorkoutRoutineRepository {

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

        val pastSessions = rehabSessionRepository.getRehabHistory(userId).first()

        val recommendationParams = RecommendationParams(
            userId = user.id, age = user.age, gender = user.gender,
            heightCm = user.heightCm, weightKg = user.weightKg,
            activityLevel = user.activityLevel, fitnessGoal = user.fitnessGoal,
            dietaryPreferences = user.preferredDietaryTypes,
            allergies = user.allergyInfo, equipmentAvailable = user.equipmentAvailable,
            currentPainLevel = user.currentPainLevel,
            injuryArea = injury?.bodyPart, injuryType = injury?.name,
            injurySeverity = injury?.severity, additionalNotes = user.additionalNotes,
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
            recommendedDiets = emptyList(),
            overallSummary = workouts.firstOrNull()?.exercises?.firstOrNull()?.aiRecommendationReason
        )
    }
}