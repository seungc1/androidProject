package com.example.androidproject.data.local.datasource

import com.example.androidproject.data.local.dao.DietSessionDao
import com.example.androidproject.data.local.dao.ExerciseDao
import com.example.androidproject.data.local.dao.RehabSessionDao
import com.example.androidproject.data.local.dao.UserDao
import com.example.androidproject.data.local.entity.DietSessionEntity
import com.example.androidproject.data.local.entity.ExerciseEntity
import com.example.androidproject.data.local.entity.RehabSessionEntity
import com.example.androidproject.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject

/**
 * [수정 파일 8/8] 'LocalDataSource'
 * (★ 추가 ★) 'getUserCountById' '함수' '추가'
 */
class LocalDataSource @Inject constructor(
    private val userDao: UserDao,
    private val exerciseDao: ExerciseDao,
    private val rehabSessionDao: RehabSessionDao,
    private val dietSessionDao: DietSessionDao
) {

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
}