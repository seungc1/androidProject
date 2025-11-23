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
        val forceApiCall: Boolean

        // ----------------------- [성능 측정 시작] -----------------------
        android.util.Log.d("REPO_PERF", "--- getWorkoutRoutine 시작 (forceReload: $forceReload) ---")
        val startTime = System.currentTimeMillis()
        // ----------------------------------------------------------------

        if (forceReload) {
            forceApiCall = true
        } else {
            // 2. 강제 리로드가 아니면 로컬 캐시 확인
            val localCache = localDataSource.getWorkouts(userId).first()
            val localDietCache = localDataSource.getScheduledDiets(userId).first()

            // ----------------------- [성능 측정 로그] -----------------------
            val cacheCheckTime = System.currentTimeMillis()
            android.util.Log.d("REPO_PERF", "1. 로컬 캐시 확인 완료: ${cacheCheckTime - startTime}ms")
            // ----------------------------------------------------------------

            // 운동 데이터가 유효하지 않은지 확인하는 로직
            val hasInvalidData = localCache.any { workout ->
            // 운동 JSON에 유효하지 않은 플래그가 포함되어 있는지 확인
                workout.exercisesJson.contains("(Day")
            }

            // [Auto-Refill Logic] 미래의 식단 개수 확인
            val dateFormat = java.text.SimpleDateFormat("M월 d일 (E)", java.util.Locale.KOREA)
            val todayCal = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }

            val futureDietsCount = localDietCache.count { diet ->
                try {
                    val date = dateFormat.parse(diet.scheduledDate)
                    if (date != null) {
                        val dietCal = java.util.Calendar.getInstance().apply { time = date }
                        // 연도 보정 (12월 -> 1월 넘어가는 경우 등) - 간단히 현재 연도 또는 내년으로 가정
                        val currentYear = todayCal.get(java.util.Calendar.YEAR)
                        val currentMonth = todayCal.get(java.util.Calendar.MONTH)
                        val dietMonth = dietCal.get(java.util.Calendar.MONTH)

                        var dietYear = currentYear
                        if (currentMonth == 11 && dietMonth == 0) {
                            dietYear += 1
                        } else if (currentMonth == 0 && dietMonth == 11) {
                            dietYear -= 1 // 혹시 과거 데이터일 경우
                        }
                        dietCal.set(java.util.Calendar.YEAR, dietYear)

                        !dietCal.before(todayCal) // 오늘 포함 미래
                    } else false
                } catch (e: Exception) {
                    false
                }
            }
            
            val needRefill = futureDietsCount < 2

            // [수정] localCache의 크기가 3 이상이고 유효한 데이터일 때만 캐시 히트로 판단합니다.
            if (localCache.size >= 3 && localDietCache.size >= 3 && !hasInvalidData) {
                emit(AIRecommendationResult(
                    scheduledWorkouts = localCache.toDomainWorkouts(),
                    scheduledDiets = localDietCache.toDomainDiets(),
                    overallSummary = "로컬 캐시"
                ))
                android.util.Log.d("REPO_PERF", "2. 캐시 히트! 총 소요 시간: ${System.currentTimeMillis() - startTime}ms")
                return@flow // 로컬 캐시 있으면 즉시 반환 (가장 빠른 경로)
            } else {
                if (needRefill) {
                    android.util.Log.d("REPO_AUTO_REFILL", "식단 데이터 부족 (남은 일수: $futureDietsCount). AI 재요청을 시작합니다.")
                }
            }

            // 캐시 미스 (사이즈 부족 또는 유효하지 않은 데이터)인 경우
            android.util.Log.d("REPO_PERF", "2. 로컬 캐시 미스 (size < 3 또는 invalid). 서버 캐시 확인 시작...")

            // --- Firebase 원격 캐시 확인 로직 ---
            try {
                val remoteWorkouts = firebaseDataSource.getWorkouts(userId)
                val remoteDiets = firebaseDataSource.getScheduledDiets(userId)

                val hasInvalidRemoteData = remoteWorkouts.any { workout ->
                    workout.exercises.any { it.name.contains("(Day") }
                }

                // [수정] remoteWorkouts의 크기가 3 이상이고 유효한 데이터일 때 캐시 히트로 판단합니다.
                if (remoteWorkouts.size >= 3 && remoteDiets.size >= 3 && !hasInvalidRemoteData) {
                    // 서버 캐시가 있다면 로컬 DB에 저장하고 반환합니다.
                    localDataSource.upsertWorkouts(remoteWorkouts.toWorkoutEntity(userId))
                    localDataSource.upsertScheduledDiets(remoteDiets.toDietEntity(userId))

                    emit(AIRecommendationResult(
                        scheduledWorkouts = remoteWorkouts,
                        scheduledDiets = remoteDiets,
                        overallSummary = "서버에서 불러옴"
                    ))
                    android.util.Log.d("REPO_PERF", "2. 서버 캐시 히트! 총 소요 시간: ${System.currentTimeMillis() - startTime}ms")
                    return@flow
                }
            } catch (e: Exception) {
                Log.e("WorkoutRepo", "Remote Cache Read Failed: ${e.message}")
            }

            forceApiCall = true // 로컬/원격 캐시 모두 미스 (size < 3)
        }

        // [자동 동기화] forceReload가 true이거나 캐시 미스(size < 3) 시, API 호출 전에 캐시를 비웁니다.
        if (forceApiCall) {
            android.util.Log.d("DEBUG_DELETE", "Repository: [API CALL NECESSARY] 로컬/서버 데이터 삭제 시도")
            // 로컬 데이터 삭제
            localDataSource.clearScheduledWorkouts(userId)
            localDataSource.clearScheduledDiets(userId)

            // 서버 데이터 삭제
            try {
                firebaseDataSource.clearWorkouts(userId)
                firebaseDataSource.clearScheduledDiets(userId)
            } catch (e: Exception) {
                android.util.Log.e("DEBUG_DELETE", "Repository: 서버 삭제 중 에러 발생: ${e.message}")
            }
        }

        // 3. AI에게 새 루틴 요청
        val pastSessions = rehabSessionRepository.getRehabHistory(userId).first()
        val aiStartTime = System.currentTimeMillis()

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

                    // 로컬 저장 (이 시점에 캐시가 이미 삭제되었으므로, 새 루틴으로 완전히 교체됩니다.)
                    localDataSource.upsertWorkouts(entities)
                    localDataSource.upsertScheduledDiets(aiResult.scheduledDiets.toDietEntity(userId))

                    // 서버 저장
                    try {
                        firebaseDataSource.upsertWorkouts(userId, aiResult.scheduledWorkouts)
                        firebaseDataSource.upsertScheduledDiets(userId, aiResult.scheduledDiets)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    // ----------------------- [성능 측정 로그] -----------------------
                    android.util.Log.d("REPO_PERF", "3. AI 요청 및 처리 완료. 소요 시간: ${System.currentTimeMillis() - aiStartTime}ms")
                    // ----------------------------------------------------------------
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
            val exercisesArray = gson.fromJson(it.exercisesJson, Array<ExerciseRecommendation>::class.java)
            ScheduledWorkout(
                scheduledDate = it.scheduledDate,
                exercises = exercisesArray?.toList() ?: emptyList()
            )
        }
        return workouts
    }

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
            val mealsArray = gson.fromJson(it.dietsJson, Array<DietRecommendation>::class.java)
            ScheduledDiet(
                scheduledDate = it.scheduledDate,
                meals = mealsArray?.toList() ?: emptyList()
            )
        }
    }
}