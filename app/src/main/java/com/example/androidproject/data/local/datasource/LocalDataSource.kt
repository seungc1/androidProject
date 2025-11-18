package com.example.androidproject.data.local.datasource

import com.example.androidproject.data.local.AppDatabase
import com.example.androidproject.data.local.dao.* // 👈 [수정] Wildcard import
import com.example.androidproject.data.local.entity.* // 👈 [수정] Wildcard import
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject

/**
 * (★수정★)
 * Hilt가 모든 DAO 6개(User, Exercise, RehabSession, DietSession, Injury, Diet)를
 * 생성자에 주입합니다.
 */
class LocalDataSource @Inject constructor(
    private val database: AppDatabase,
    private val userDao: UserDao,
    private val exerciseDao: ExerciseDao,
    private val rehabSessionDao: RehabSessionDao,
    private val dietSessionDao: DietSessionDao,
    private val injuryDao: InjuryDao,           // 👈 🚨 [추가]
    private val dietDao: DietDao,               // 👈 🚨 [추가]
    private val scheduledWorkoutDao: ScheduledWorkoutDao // 👈 🚨 [추가]
) {
    suspend fun clearAllData() {
        database.clearAllTables()
    }

    // --- UserDao 관련 함수 ---
    suspend fun upsertUser(user: UserEntity) {
        userDao.upsertUser(user)
    }
    fun getUserById(userId: String): Flow<UserEntity?> {
        return userDao.getUserById(userId)
    }

    // (★ 추가 ★) '아이디' '중복' '확인' '통로'
    suspend fun getUserCountById(id: String): Int {
        return userDao.getUserCountById(id)
    }

    // --- (이하 Exercise, Rehab, Diet '관련' '함수' '수정' '없음') ---

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
    suspend fun addRehabSession(session: RehabSessionEntity) {
        rehabSessionDao.addRehabSession(session)
    }
    fun getRehabHistory(userId: String): Flow<List<RehabSessionEntity>> {
        return rehabSessionDao.getRehabHistory(userId)
    }
    fun getRehabSessionsBetween(userId: String, startDate: Date, endDate: Date): Flow<List<RehabSessionEntity>> {
        return rehabSessionDao.getSessionsBetween(userId, startDate, endDate)
    }

    // --- DietSessionDao 관련 함수 ---
    suspend fun addDietSession(session: DietSessionEntity) {
        dietSessionDao.addDietSession(session)
    }
    fun getDietHistory(userId: String): Flow<List<DietSessionEntity>> {
        return dietSessionDao.getDietHistory(userId)
    }
    fun getDietSessionsBetween(userId: String, startDate: Date, endDate: Date): Flow<List<DietSessionEntity>> {
        return dietSessionDao.getSessionsBetween(userId, startDate, endDate)
    }

    // 🚨 [추가] --- InjuryDao 관련 함수 ---
    suspend fun upsertInjury(injury: InjuryEntity) {
        injuryDao.upsertInjury(injury)
    }
    fun getInjuryById(injuryId: String): Flow<InjuryEntity?> {
        return injuryDao.getInjuryById(injuryId)
    }
    // 🚨 [오류 해결] 'getInjuriesForUser' 함수를 추가합니다.
    fun getInjuriesForUser(userId: String): Flow<List<InjuryEntity>> {
        return injuryDao.getInjuriesForUser(userId)
    }

    // 🚨 [추가] --- DietDao 관련 함수 ---
    suspend fun upsertDiets(diets: List<DietEntity>) {
        dietDao.upsertDiets(diets)
    }
    fun getDietById(dietId: String): Flow<DietEntity?> {
        return dietDao.getDietById(dietId)
    }

    // 🚨 [추가] --- ScheduledWorkoutDao 관련 함수 ---
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