// 파일 경로: app/src/main/java/com/example/androidproject/data/repository/WorkoutRoutineRepositoryImpl.kt
package com.example.androidproject.data.repository

import android.util.Log
import com.example.androidproject.data.local.datasource.LocalDataSource
import com.example.androidproject.data.local.entity.ScheduledWorkoutEntity
import com.example.androidproject.data.remote.datasource.FirebaseDataSource
import com.example.androidproject.domain.model.*
import com.example.androidproject.domain.repository.AIApiRepository
import com.example.androidproject.domain.repository.RehabSessionRepository
import com.example.androidproject.domain.repository.WorkoutRoutineRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class WorkoutRoutineRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDataSource,
    private val firebaseDataSource: FirebaseDataSource,
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
            localDataSource.clearScheduledWorkouts(userId)
            try {
                firebaseDataSource.clearWorkouts(userId)
            } catch (e: Exception) {
                Log.e("WorkoutRepo", "Failed to clear remote workouts on forceReload: ${e.message}")
            }
        }

        // 1. 로컬 캐시 확인
        val dbCache = localDataSource.getWorkouts(userId).first()
        if (dbCache.isNotEmpty() && !forceReload) {
            emit(dbCache.toDomainResult())
            return@flow
        }

        // 2. 로컬에 없으면 -> 서버(Firebase) 확인
        try {
            val remoteWorkouts = firebaseDataSource.getWorkouts(userId)
            if (remoteWorkouts.isNotEmpty()) {
                localDataSource.upsertWorkouts(remoteWorkouts.toEntity(userId))
                emit(AIRecommendationResult(remoteWorkouts, emptyList(), "서버에서 불러옴"))
                return@flow
            }
        } catch (e: Exception) {
            Log.e("WorkoutRepo", "Remote Cache Read Failed: ${e.message}")
        }

        // 3. AI에게 새 루틴 요청 (기존 로직)
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
                val entities = aiResult.toEntity(userId)
                localDataSource.upsertWorkouts(entities)

                try {
                    firebaseDataSource.upsertWorkouts(userId, aiResult.scheduledWorkouts)
                } catch (e: Exception) {
                    Log.e("WorkoutRepo", "Firebase Save Failed: ${e.message}")
                }

                emit(localDataSource.getWorkouts(userId).first().toDomainResult())
            }
    }

    /**
     * ★★★ [구현] 운동 완료 상태를 Local DB와 Firebase에 저장합니다. ★★★
     */
    override suspend fun upsertWorkoutRoutineState(userId: String, updatedFullRoutine: List<ScheduledWorkout>) {
        val entities = updatedFullRoutine.toEntity(userId)

        // 1. Local DB에 저장 (앱 재실행 시 체크 상태 유지)
        localDataSource.upsertWorkouts(entities)

        // 2. Firebase에 저장 (다중 기기 및 영구 저장소 유지)
        try {
            firebaseDataSource.upsertWorkouts(userId, updatedFullRoutine)
        } catch (e: Exception) {
            Log.e("WorkoutRepo", "Firebase State Update Failed: ${e.message}")
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

    private fun List<ScheduledWorkout>.toEntity(userId: String): List<ScheduledWorkoutEntity> {
        return this.map {
            ScheduledWorkoutEntity(
                userId = userId,
                scheduledDate = it.scheduledDate,
                exercisesJson = Gson().toJson(it.exercises)
            )
        }
    }

    private fun List<ScheduledWorkoutEntity>.toDomainResult(): AIRecommendationResult {
        val gson = Gson()
        val exercisesListType = object : TypeToken<List<ExerciseRecommendation>>() {}.type

        val workouts = this.map {
            val exercises = gson.fromJson<List<ExerciseRecommendation>>(it.exercisesJson, exercisesListType)

            ScheduledWorkout(
                scheduledDate = it.scheduledDate,
                exercises = exercises ?: emptyList()
            )
        }
        return AIRecommendationResult(
            scheduledWorkouts = workouts,
            recommendedDiets = emptyList(),
            overallSummary = workouts.firstOrNull()?.exercises?.firstOrNull()?.aiRecommendationReason
        )
    }
}