package com.example.androidproject.data.local.datasource

import com.example.androidproject.data.local.dao.* // 👈 [수정] Wildcard import
import com.example.androidproject.data.local.entity.* // 👈 [수정] Wildcard import
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject

/**
 * 모든 DAO를 실제로 호출하여 Local(로컬 DB) 데이터를 관리하는 클래스입니다.
 * Hilt가 모든 DAO를 여기에 주입(@Inject)해 줍니다.
 */
// ✅ [수정] 생성자에서 새로 만든 DAO 2개를 주입받습니다.
class LocalDataSource @Inject constructor(
    private val userDao: UserDao,
    private val exerciseDao: ExerciseDao,
    private val rehabSessionDao: RehabSessionDao,
    private val dietSessionDao: DietSessionDao,
    private val scheduledWorkoutDao: ScheduledWorkoutDao
) {

    // --- UserDao 관련 함수 ---
    suspend fun upsertUser(user: UserEntity) {
        userDao.upsertUser(user)
    }
    fun getUserById(userId: String): Flow<UserEntity?> {
        return userDao.getUserById(userId)
    }

    // --- ExerciseDao 관련 함수 ---
    suspend fun upsertExercises(exercises: List<ExerciseEntity>) {
        exerciseDao.upsertExercises(exercises)
    }
    fun getExercisesByBodyPart(bodyPart: String): Flow<List<ExerciseEntity>> {
        return exerciseDao.getExercisesByBodyPart(bodyPart)
    }
    fun getExerciseById(exerciseId: String): Flow<ExerciseEntity?> {
        return exerciseDao.getExerciseById(exerciseId)
    }

    // --- RehabSessionDao 관련 함수 ---

    /**
     * RehabSessionDao에 '운동 세션 추가'를 요청합니다.
     */
    suspend fun addRehabSession(session: RehabSessionEntity) { // ✅ [추가]
        rehabSessionDao.addRehabSession(session)
    }

    /**
     * RehabSessionDao에 '운동 기록 조회'를 요청합니다.
     */
    fun getRehabHistory(userId: String): Flow<List<RehabSessionEntity>> { // ✅ [추가]
        return rehabSessionDao.getRehabHistory(userId)
    }

    /**
     * RehabSessionDao에 '기간별 운동 기록 조회'를 요청합니다. (UseCase용)
     */
    fun getRehabSessionsBetween(userId: String, startDate: Date, endDate: Date): Flow<List<RehabSessionEntity>> { // ✅ [추가]
        return rehabSessionDao.getSessionsBetween(userId, startDate, endDate)
    }

    // --- DietSessionDao 관련 함수 ---

    /**
     * DietSessionDao에 '식단 세션 추가'를 요청합니다.
     */
    suspend fun addDietSession(session: DietSessionEntity) { // ✅ [추가]
        dietSessionDao.addDietSession(session)
    }

    /**
     * DietSessionDao에 '식단 기록 조회'를 요청합니다.
     */
    fun getDietHistory(userId: String): Flow<List<DietSessionEntity>> { // ✅ [추가]
        return dietSessionDao.getDietHistory(userId)
    }

    /**
     * DietSessionDao에 '기간별 식단 기록 조회'를 요청합니다. (UseCase용)
     */
    fun getDietSessionsBetween(userId: String, startDate: Date, endDate: Date): Flow<List<DietSessionEntity>> { // ✅ [추가]
        return dietSessionDao.getSessionsBetween(userId, startDate, endDate)
    }
    suspend fun upsertWorkouts(workouts: List<ScheduledWorkoutEntity>) {
        scheduledWorkoutDao.upsertWorkouts(workouts)
    }

    fun getWorkouts(userId: String): Flow<List<ScheduledWorkoutEntity>> {
        return scheduledWorkoutDao.getWorkouts(userId)
    }

    suspend fun clearWorkouts(userId: String) {
        scheduledWorkoutDao.clearWorkouts(userId)
    }
}