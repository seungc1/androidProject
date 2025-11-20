package com.example.androidproject.data.repository

import android.util.Log
import com.example.androidproject.data.local.datasource.LocalDataSource
import com.example.androidproject.data.local.entity.ScheduledDietEntity // ✅ [추가]
import com.example.androidproject.data.local.entity.ScheduledWorkoutEntity
import com.example.androidproject.data.remote.datasource.FirebaseDataSource
import com.example.androidproject.domain.model.*
import com.example.androidproject.domain.repository.AIApiRepository
import com.example.androidproject.domain.repository.RehabSessionRepository
import com.example.androidproject.domain.repository.WorkoutRoutineRepository
import com.google.gson.Gson
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
            localDataSource.clearWorkouts(userId)
            localDataSource.clearScheduledDiets(userId) // ✅ [추가] 식단 캐시 삭제
            try {
                firebaseDataSource.clearWorkouts(userId)
                // (나중에 FirebaseDataSource에 식단 삭제 기능 추가 시 호출)
                Log.d("WorkoutRepo", "Cleared remote and local data due to forceReload.")
            } catch (e: Exception) {
                Log.e("WorkoutRepo", "Failed to clear remote data: ${e.message}")
            }
        } else {
            // 2. 로컬 캐시 확인 (운동 + 식단)
            val localWorkouts = localDataSource.getWorkouts(userId).first()
            val localDiets = localDataSource.getScheduledDiets(userId).first() // ✅ [추가] 식단 조회

            // 운동과 식단 데이터가 모두 있으면 캐시 반환
            if (localWorkouts.isNotEmpty()) {
                emit(
                    AIRecommendationResult(
                        scheduledWorkouts = localWorkouts.toDomainWorkouts(),
                        scheduledDiets = localDiets.toDomainDiets(), // ✅ [추가] 변환하여 반환
                        overallSummary = "최근 저장된 루틴을 불러왔습니다.",
                        disclaimer = ""
                    )
                )
                return@flow
            }

            // (참고: Firebase 캐시 확인 로직은 운동만 되어 있으므로 일단 넘어갑니다)
        }

        // 3. AI에게 새 루틴 요청
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
                Log.d("WorkoutRepo", "AI Generated: ${aiResult.scheduledWorkouts.size} workout days, ${aiResult.scheduledDiets.size} diet days")

                // 4. 결과 저장 (운동 + 식단)
                if (aiResult.scheduledWorkouts.isNotEmpty()) {
                    // A. 운동 저장
                    localDataSource.upsertWorkouts(aiResult.scheduledWorkouts.toWorkoutEntity(userId))

                    // B. ✅ [추가] 식단 저장
                    if (aiResult.scheduledDiets.isNotEmpty()) {
                        localDataSource.upsertScheduledDiets(aiResult.scheduledDiets.toDietEntity(userId))
                    }

                    // C. Firebase 저장 (운동만 구현됨)
                    try {
                        firebaseDataSource.upsertWorkouts(userId, aiResult.scheduledWorkouts)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                // 5. 저장된 데이터 다시 조회해서 내보냄 (UI 갱신)
                val savedWorkouts = localDataSource.getWorkouts(userId).first()
                val savedDiets = localDataSource.getScheduledDiets(userId).first() // ✅ [추가]

                emit(
                    AIRecommendationResult(
                        scheduledWorkouts = savedWorkouts.toDomainWorkouts(),
                        scheduledDiets = savedDiets.toDomainDiets(), // ✅ [추가]
                        overallSummary = aiResult.overallSummary,
                        disclaimer = aiResult.disclaimer
                    )
                )
            }
    }

    // --- Mapper 함수들 ---

    // 1. 운동 관련 Mapper (기존 유지, 이름만 명확하게 변경)
    private fun List<ScheduledWorkout>.toWorkoutEntity(userId: String): List<ScheduledWorkoutEntity> {
        return this.map {
            ScheduledWorkoutEntity(
                userId = userId,
                scheduledDate = it.scheduledDate,
                exercisesJson = Gson().toJson(it.exercises)
            )
        }
    }

    private fun List<ScheduledWorkoutEntity>.toDomainWorkouts(): List<ScheduledWorkout> {
        val gson = Gson()
        return this.map {
            val exercisesArray = gson.fromJson(it.exercisesJson, Array<ExerciseRecommendation>::class.java)
            ScheduledWorkout(
                scheduledDate = it.scheduledDate,
                exercises = exercisesArray?.toList() ?: emptyList()
            )
        }
    }

    // 2. ✅ [추가] 식단 관련 Mapper
    private fun List<ScheduledDiet>.toDietEntity(userId: String): List<ScheduledDietEntity> {
        return this.map {
            ScheduledDietEntity(
                userId = userId,
                scheduledDate = it.scheduledDate,
                dietsJson = Gson().toJson(it.meals)
            )
        }
    }

    private fun List<ScheduledDietEntity>.toDomainDiets(): List<ScheduledDiet> {
        val gson = Gson()
        return this.map {
            // JSON 문자열 -> DietRecommendation 배열 -> List
            val mealsArray = gson.fromJson(it.dietsJson, Array<DietRecommendation>::class.java)
            ScheduledDiet(
                scheduledDate = it.scheduledDate,
                meals = mealsArray?.toList() ?: emptyList()
            )
        }
    }
}