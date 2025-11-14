package com.example.androidproject.data.local.datasource

import com.example.androidproject.data.local.dao.ExerciseDao
import com.example.androidproject.data.local.dao.UserDao
import com.example.androidproject.data.local.entity.ExerciseEntity
import com.example.androidproject.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 'UserDao'와 'ExerciseDao'를 실제로 호출하여 Local(로컬 DB) 데이터를 관리하는 클래스입니다.
 * (상세 로드맵 Phase 1)
 *
 * Hilt가 'UserDao'와 'ExerciseDao'를 여기에 주입(@Inject)해 줍니다.
 */
class LocalDataSource @Inject constructor(
    private val userDao: UserDao,
    private val exerciseDao: ExerciseDao
) {

    // --- UserDao 관련 함수 ---

    /**
     * 유저 정보를 DB에 삽입/업데이트합니다.
     */
    suspend fun upsertUser(user: UserEntity) {
        userDao.upsertUser(user)
    }

    /**
     * ID로 유저 정보를 Flow 형태로 조회합니다.
     */
    fun getUserById(userId: String): Flow<UserEntity?> {
        return userDao.getUserById(userId)
    }

    // --- ExerciseDao 관련 함수 ---

    /**
     * 운동 목록을 DB에 삽입/업데이트합니다.
     */
    suspend fun upsertExercises(exercises: List<ExerciseEntity>) {
        exerciseDao.upsertExercises(exercises)
    }

    /**
     * 신체 부위로 운동 목록을 Flow 형태로 조회합니다.
     */
    fun getExercisesByBodyPart(bodyPart: String): Flow<List<ExerciseEntity>> {
        return exerciseDao.getExercisesByBodyPart(bodyPart)
    }
}