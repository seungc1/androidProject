// seungc1/androidproject/androidProject-dev/app/src/main/java/com/example/androidproject/data/repository/WorkoutRoutineRepositoryImpl.kt

package com.example.androidproject.data.repository

import android.util.Log
import com.example.androidproject.data.local.datasource.LocalDataSource
import com.example.androidproject.data.local.entity.ScheduledDietEntity
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

        // 1. [핵심] 상태 변경(forceReload) 시 기존 데이터 삭제 후 AI 재요청
        if (forceReload) {
            android.util.Log.d("DEBUG_DELETE", "Repository: [1단계] 로컬 데이터 삭제 시도")
            localDataSource.clearScheduledWorkouts(userId)
            android.util.Log.d("DEBUG_DELETE", "Repository: [1단계] 로컬 데이터 삭제 완료")

            try {
                android.util.Log.d("DEBUG_DELETE", "Repository: [2단계] 서버 데이터 삭제 시도")
                firebaseDataSource.clearWorkouts(userId)
                android.util.Log.d("DEBUG_DELETE", "Repository: [2단계] 서버 데이터 삭제 완료")
            } catch (e: Exception) {
                android.util.Log.e("DEBUG_DELETE", "Repository: 서버 삭제 중 에러 발생: ${e.message}")
            }

            // [추가] 식단 데이터도 삭제
            localDataSource.clearScheduledDiets(userId)
            try {
                firebaseDataSource.clearScheduledDiets(userId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            // 2. 강제 리로드가 아니면 로컬 캐시 확인
            val localCache = localDataSource.getWorkouts(userId).first()
            val localDietCache = localDataSource.getScheduledDiets(userId).first()

            // 운동 데이터와 식단 데이터가 *모두* 있어야 유효한 캐시로 인정
            val hasInvalidData = localCache.any { workout ->
                workout.exercisesJson.contains("(Day")
            }

            if (localCache.isNotEmpty() && localDietCache.size >= 7 && !hasInvalidData) {
                emit(AIRecommendationResult(
                    scheduledWorkouts = localCache.toDomainWorkouts(),
                    scheduledDiets = localDietCache.toDomainDiets(),
                    overallSummary = "로컬 캐시"
                ))
                return@flow // 로컬 캐시 있으면 즉시 반환 (가장 빠른 경로)
            }

            // >>> 로딩 속도 개선을 위해 Firebase 원격 캐시 확인 로직이 삭제되었습니다. <<<
        }

        // 3. AI에게 새 루틴 요청 (로컬 캐시가 없거나, forceReload인 경우 실행됨)
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
                    val entities = aiResult.scheduledWorkouts.toWorkoutEntity(userId)

                    // 로컬 저장 (동기적으로 실행됨)
                    localDataSource.upsertWorkouts(entities)
                    localDataSource.upsertScheduledDiets(aiResult.scheduledDiets.toDietEntity(userId))

                    // 서버 저장 (비동기 - 실패해도 로컬은 저장됨)
                    try {
                        firebaseDataSource.upsertWorkouts(userId, aiResult.scheduledWorkouts)
                        firebaseDataSource.upsertScheduledDiets(userId, aiResult.scheduledDiets)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    emit(aiResult)

                } else {
                    Log.w("WorkoutRepo", "AI returned empty workouts.")
                    val localBackup = localDataSource.getWorkouts(userId).first()
                    val localDietBackup = localDataSource.getScheduledDiets(userId).first()

                    if (localBackup.isNotEmpty()) {
                        emit(AIRecommendationResult(
                            scheduledWorkouts = localBackup.toDomainWorkouts(),
                            scheduledDiets = localDietBackup.toDomainDiets(),
                            overallSummary = "로컬 백업"
                        ))
                    } else {
                        // 진짜 아무것도 없으면 빈 화면
                        emit(AIRecommendationResult(emptyList(), emptyList(), "데이터를 불러올 수 없습니다."))
                    }
                }
            }
    }
    // --- Mapper 함수들 ---

    // 1. 운동 관련 Mapper
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
        val workouts = this.map {
            // R8/ProGuard 오류 방지를 위해 TypeToken 대신 Array로 받아서 toList()로 변환
            val exercisesArray = gson.fromJson(it.exercisesJson, Array<ExerciseRecommendation>::class.java)
            ScheduledWorkout(
                scheduledDate = it.scheduledDate,
                exercises = exercisesArray?.toList() ?: emptyList()
            )
        }
        return workouts
    }

    // 2. 식단 관련 Mapper
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