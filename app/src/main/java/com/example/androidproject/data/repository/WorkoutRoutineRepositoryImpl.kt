// 파일 경로: app/src/main/java/com/example/androidproject/data/repository/WorkoutRoutineRepositoryImpl.kt
package com.example.androidproject.data.repository

import android.util.Log // ★★★ 이 구문이 누락되었을 수 있습니다. ★★★
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

        // 1. 강제 리로드 시 처리
        if (forceReload) {
            // 로컬/서버 캐시 삭제 (새로 받을 거니까)
            localDataSource.clearWorkouts(userId) // <-- LocalDataSource의 clearWorkouts 호출
            try {
                firebaseDataSource.clearWorkouts(userId)
                Log.d("WorkoutRepo", "Cleared remote and local workouts due to forceReload.")
            } catch (e: Exception) {
                Log.e("WorkoutRepo", "Failed to clear remote workouts: ${e.message}")
            }
        } else {
            // 2. 강제 리로드 X -> 로컬/서버 캐시 확인

            // A. 로컬 캐시 확인
            val localCache = localDataSource.getWorkouts(userId).first()
            if (localCache.isNotEmpty()) {
                emit(localCache.toDomainResult())
                return@flow
            }

            // B. 로컬 캐시 X -> 서버 캐시 확인
            try {
                val remoteWorkouts = firebaseDataSource.getWorkouts(userId)
                if (remoteWorkouts.isNotEmpty()) {
                    // 서버에 있으면 로컬에 저장하고 반환 (화면 갱신)
                    localDataSource.upsertWorkouts(remoteWorkouts.toEntity(userId))
                    emit(AIRecommendationResult(remoteWorkouts, emptyList(), "서버 캐시에서 불러온 루틴입니다."))
                    return@flow
                }
            } catch (e: Exception) {
                Log.e("WorkoutRepo", "Remote Cache Read Failed: ${e.message}")
                // 서버 에러나도 AI 요청으로 진행
            }
        }

        // 3. AI에게 새 루틴 요청 (캐시가 없거나, forceReload인 경우)
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
                Log.d("WorkoutRepo", "AI Generated Days: ${aiResult.scheduledWorkouts.size}")

                // 4. 결과 저장 (로컬 + 서버)
                if (aiResult.scheduledWorkouts.isNotEmpty()) {
                    val entities = aiResult.toEntity(userId)
                    localDataSource.upsertWorkouts(entities)

                    try {
                        // 서버에 저장 (새 루틴이므로)
                        firebaseDataSource.upsertWorkouts(userId, aiResult.scheduledWorkouts)
                        Log.d("WorkoutRepo", "Firebase Save Success: New routine stored.")
                    } catch (e: Exception) {
                        Log.e("WorkoutRepo", "Firebase Save Failed: ${e.message}")
                        e.printStackTrace()
                    }
                }

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
            // R8/ProGuard 오류 방지를 위해 TypeToken 대신 Array로 받아서 toList()로 변환
            val exercisesArray = gson.fromJson(it.exercisesJson, Array<ExerciseRecommendation>::class.java)

            ScheduledWorkout(
                scheduledDate = it.scheduledDate,
                exercises = exercisesArray?.toList() ?: emptyList()
            )
        }
        return AIRecommendationResult(
            scheduledWorkouts = workouts,
            recommendedDiets = emptyList(),
            overallSummary = workouts.firstOrNull()?.exercises?.firstOrNull()?.aiRecommendationReason
        )
    }
}