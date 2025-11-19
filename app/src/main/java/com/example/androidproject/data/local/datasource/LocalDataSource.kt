package com.example.androidproject.data.local.datasource

import com.example.androidproject.data.local.AppDatabase
import com.example.androidproject.data.local.dao.*
import com.example.androidproject.data.local.entity.*
import kotlinx.coroutines.Dispatchers // (★필수★) 스레드 전환을 위해 import
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext // (★필수★) 스레드 전환을 위해 import
import java.util.Date
import javax.inject.Inject

/**
 * (★수정★) clearAllData 함수에 스레드 처리 추가
 */
class LocalDataSource @Inject constructor(
    private val database: AppDatabase,
    private val userDao: UserDao,
    private val exerciseDao: ExerciseDao,
    private val rehabSessionDao: RehabSessionDao,
    private val dietSessionDao: DietSessionDao,
    private val injuryDao: InjuryDao,
    private val dietDao: DietDao,
    private val scheduledWorkoutDao: ScheduledWorkoutDao
) {
    /**
     * (★수정★) DB 전체 삭제 (로그아웃 시 호출됨)
     * 메인 스레드에서 호출하면 크래시가 나므로, IO 스레드로 전환하여 실행합니다.
     */
    suspend fun clearAllData() {
        withContext(Dispatchers.IO) {
            database.clearAllTables()
        }
    }

    // --- UserDao 관련 함수 ---
    suspend fun upsertUser(user: UserEntity) {
        userDao.upsertUser(user)
    }
    fun getUserById(userId: String): Flow<UserEntity?> {
        return userDao.getUserById(userId)
    }

    suspend fun getUserCountById(id: String): Int {
        return userDao.getUserCountById(id)
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

    // --- InjuryDao 관련 함수 ---
    suspend fun upsertInjury(injury: InjuryEntity) {
        injuryDao.upsertInjury(injury)
    }
    fun getInjuryById(injuryId: String): Flow<InjuryEntity?> {
        return injuryDao.getInjuryById(injuryId)
    }
    fun getInjuriesForUser(userId: String): Flow<List<InjuryEntity>> {
        return injuryDao.getInjuriesForUser(userId)
    }

    // --- DietDao 관련 함수 ---
    suspend fun upsertDiets(diets: List<DietEntity>) {
        dietDao.upsertDiets(diets)
    }
    fun getDietById(dietId: String): Flow<DietEntity?> {
        return dietDao.getDietById(dietId)
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