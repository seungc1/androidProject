package com.example.androidproject.data.repository

import com.example.androidproject.data.local.datasource.LocalDataSource
import com.example.androidproject.data.mapper.toDomain
import com.example.androidproject.data.mapper.toEntity
import com.example.androidproject.domain.model.User
import com.example.androidproject.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * [수정 파일 9/8] 'UserRepository' '구현체'
 * (★ 수정 ★) 'login' '함수'에서 '자동' '회원가입' '로직' '제거'
 * (★ 추가 ★) 'checkUserExists', 'createUser' '함수' '구현'
 */
class UserRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDataSource
) : UserRepository {

    /**
     * (★ 수정 ★) '자동' '회원가입' '기능' '제거'
     */
    override suspend fun login(id: String, password: String): String? {
        // 1. DB에서 '해당' 'ID'의 '유저'를 '찾습니다'.
        val userEntity = localDataSource.getUserById(id).first()

        return if (userEntity == null) {
            // 2-A. '유저'가 '없으면' -> '로그인' '실패' ('null' '반환')
            null
        } else {
            // 2-B. '유저'가 '있으면' -> '비밀번호' '확인'
            if (userEntity.password == password) {
                // '성공' '시' 'userId' '반환'
                userEntity.id
            } else {
                // '실패' '시' 'null' '반환'
                null
            }
        }
    }

    // (getUserProfile '함수' - 수정 없음)
    override suspend fun getUserProfile(userId: String): Flow<User> {
        return localDataSource.getUserById(userId).map { userEntity ->
            userEntity?.toDomain() ?: getTemporaryUser(userId) // (★ 수정 ★) 'ID'가 '없는' '것' '대신' '전달'받은 'ID'로 '임시' '객체' '생성'
        }
    }

    // (updateUserProfile '함수' - 수정 없음)
    override suspend fun updateUserProfile(user: User): Flow<Unit> {
        localDataSource.upsertUser(user.toEntity())
        return flowOf(Unit)
    }

    /**
     * (★ 추가 ★) '아이디' '중복' '확인' '구현'
     */
    override suspend fun checkUserExists(id: String): Boolean {
        // 'DB'에 'ID'가 '일치'하는 '사용자'가 '1명' '이상'인지 '확인'
        return localDataSource.getUserCountById(id) > 0
    }

    /**
     * (★ 추가 ★) '새' '사용자' '생성' '구현'
     */
    override suspend fun createUser(user: User): Flow<Unit> {
        // (upsert '함수'를 '그대로' '사용'하면 '됨')
        localDataSource.upsertUser(user.toEntity())
        return flowOf(Unit)
    }

    // '임시' '유저' '생성' '함수' ('ID'를 '받도록' '수정')
    private fun getTemporaryUser(userId: String): User {
        return User(
            id = userId, password = "", name = "사용자", gender = "", age = 0,
            heightCm = 0, weightKg = 0.0, activityLevel = "",
            fitnessGoal = "", allergyInfo = emptyList(),
            preferredDietType = "", targetCalories = 0,
            currentInjuryId = null, preferredDietaryTypes = emptyList(),
            equipmentAvailable = emptyList(), currentPainLevel = 0, additionalNotes = null
        )
    }
}