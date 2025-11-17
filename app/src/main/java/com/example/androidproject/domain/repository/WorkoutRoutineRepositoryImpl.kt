package com.example.androidproject.data.repository

import com.example.androidproject.data.local.datasource.LocalDataSource
import com.example.androidproject.data.local.entity.ScheduledWorkoutEntity
import com.example.androidproject.domain.model.AIRecommendationResult
import com.example.androidproject.domain.model.Injury
import com.example.androidproject.domain.model.RecommendationParams
import com.example.androidproject.domain.model.ScheduledWorkout
import com.example.androidproject.domain.model.User
import com.example.androidproject.domain.repository.AIApiRepository
import com.example.androidproject.domain.repository.WorkoutRoutineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * WorkoutRoutineRepository의 구현체.
 * '캐싱 우선' (Cache-then-network) 로직을 수행합니다.
 */
class WorkoutRoutineRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDataSource, // DB 접근
    private val aiApiRepository: AIAIApiRepository // API 접근
) : WorkoutRoutineRepository {

    override suspend fun getWorkoutRoutine(
        forceReload: Boolean,
        user: User,
        injury: Injury?
    ): Flow<AIRecommendationResult> = flow {

        val userId = user.id

        // 1. 강제 새로고침 시, DB의 기존 루틴 삭제
        if (forceReload) {
            localDataSource.clearWorkouts(userId)
        }

        // 2. DB에서 루틴 가져오기 시도
        val dbCache = localDataSource.getWorkouts(userId).first()

        // 3. DB가 비어있지 않고, 강제 새로고침이 아니면 -> DB 데이터 반환
        if (dbCache.isNotEmpty() && !forceReload) {
            emit(dbCache.toDomainResult()) // (Entity -> Domain 모델로 변환)
            return@flow
        }

        // 4. DB가 비어있거나 강제 새로고침이면 -> AI API 호출

        // 4-1. AI 요청서 준비 (기존 UseCase 로직)
        // (★참고★: 이 로직을 위해 'RehabSessionRepository'도 주입받아야 함)
        // (지금은 임시로 'emptyList()' 전달)
        val pastSessions = emptyList()

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

        // 4-2. API 호출
        aiApiRepository.getAIRehabAndDietRecommendation(recommendationParams)
            .collect { aiResult ->
                // 4-3. API 결과를 DB에 저장
                localDataSource.upsertWorkouts(aiResult.toEntity(userId))

                // 4-4. 저장된 DB 데이터를 다시 조회하여 반환 (Single Source of Truth)
                emit(localDataSource.getWorkouts(userId).first().toDomainResult())
            }
    }

    // --- Mapper 함수들 ---

    // AIRecommendationResult(Domain) -> List<ScheduledWorkoutEntity>(DB)
    private fun AIRecommendationResult.toEntity(userId: String): List<ScheduledWorkoutEntity> {
        return this.scheduledWorkouts.map {
            ScheduledWorkoutEntity(
                userId = userId,
                scheduledDate = it.scheduledDate,
                // (List<ExerciseRecommendation> -> JSON String)
                exercisesJson = Gson().toJson(it.exercises)
            )
        }
    }

    // List<ScheduledWorkoutEntity>(DB) -> AIRecommendationResult(Domain)
    private fun List<ScheduledWorkoutEntity>.toDomainResult(): AIRecommendationResult {
        val gson = Gson()
        val workouts = this.map {
            ScheduledWorkout(
                scheduledDate = it.scheduledDate,
                // (JSON String -> List<ExerciseRecommendation>)
                exercises = gson.fromJson(
                    it.exercisesJson,
                    object : com.google.gson.reflect.TypeToken<List<ExerciseRecommendation>>() {}.type
                )
            )
        }
        // (★참고★: 식단(Diet)은 이 Repository가 관리하지 않으므로 빈 목록 반환)
        return AIRecommendationResult(
            scheduledWorkouts = workouts,
            recommendedDiets = emptyList(), // (식단은 ViewModel이 따로 로드해야 함)
            overallSummary = workouts.firstOrNull()?.exercises?.firstOrNull()?.aiRecommendationReason
        )
    }
}