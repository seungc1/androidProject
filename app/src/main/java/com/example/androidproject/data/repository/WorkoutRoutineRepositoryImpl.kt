package com.example.androidproject.data.repository

import com.example.androidproject.data.local.datasource.LocalDataSource
import com.example.androidproject.data.remote.datasource.FirebaseDataSource // (★ 추가)
import com.example.androidproject.data.local.entity.ScheduledWorkoutEntity
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
    private val firebaseDataSource: FirebaseDataSource, // (★ 추가 주입)
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
            // (선택) 서버 데이터도 지우고 싶으면 firebaseDataSource.clearWorkouts(userId) 호출
        }

        // 1. 로컬 캐시 확인
        val dbCache = localDataSource.getWorkouts(userId).first()
        if (dbCache.isNotEmpty() && !forceReload) {
            emit(dbCache.toDomainResult())
            return@flow
        }

        // 2. (★추가★) 로컬에 없으면 -> 서버(Firebase) 확인 (다른 기기에서 생성했을 수도 있음)
        try {
            val remoteWorkouts = firebaseDataSource.getWorkouts(userId)
            if (remoteWorkouts.isNotEmpty()) {
                // 서버에 있으면 가져와서 로컬에 저장하고 반환
                localDataSource.upsertWorkouts(remoteWorkouts.toEntity(userId))
                emit(AIRecommendationResult(remoteWorkouts, emptyList(), "서버에서 불러온 루틴입니다.")) // 식단/요약은 별도 저장 안 했으면 비워둠
                return@flow
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // 서버 에러나도 무시하고 AI 요청으로 넘어감
        }

        // 3. 서버에도 없으면 -> AI에게 요청 (기존 로직)
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
                // 결과 받으면 로컬 + 서버 둘 다 저장
                val entities = aiResult.toEntity(userId)
                localDataSource.upsertWorkouts(entities)

                try {
                    firebaseDataSource.upsertWorkouts(userId, aiResult.scheduledWorkouts) // (★ 추가)
                } catch (e: Exception) { e.printStackTrace() }

                // 화면 갱신을 위해 로컬 데이터 다시 조회해서 내보냄
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

    // (★추가★) Domain -> Entity 변환 (서버에서 받은 데이터를 로컬에 넣을 때 사용)
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