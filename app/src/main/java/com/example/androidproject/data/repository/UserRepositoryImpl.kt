package com.example.androidproject.data.repository

import com.example.androidproject.data.local.datasource.LocalDataSource
import com.example.androidproject.data.remote.datasource.FirebaseDataSource // (★ 추가)
import com.example.androidproject.data.mapper.toDomain
import com.example.androidproject.data.mapper.toEntity
import com.example.androidproject.domain.model.User
import com.example.androidproject.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class UserRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDataSource,
    private val firebaseDataSource: FirebaseDataSource
) : UserRepository {

    /**
     * (★ 수정 ★) Firebase 로그인 사용
     */
    override suspend fun login(id: String, password: String): String? {
        return try {
            // FirebaseDataSource의 login 함수 호출
            // 성공 시 uid(String) 반환, 실패 시 Exception 발생
            firebaseDataSource.login(id, password)
        } catch (e: Exception) {
            e.printStackTrace()
            null // 로그인 실패 시 null 반환
        }
    }

    /**
     * (★ 수정 ★) Firebase 회원가입 사용
     */
    override suspend fun createUser(user: User): Flow<Unit> {
        return try {
            firebaseDataSource.signUp(user) // Firebase에 저장

            // (선택) 로컬 DB에도 백업하려면 아래 주석 해제 (지금은 충돌 방지 위해 주석)
            // localDataSource.upsertUser(user.toEntity())

            flowOf(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e // ViewModel에서 에러 잡을 수 있게 던짐
        }
    }

    // --- 아래 함수들은 로컬 DB를 계속 사용하거나, 나중에 Firebase로 마이그레이션 가능 ---

    // (기존 유지) 로컬 DB에서 프로필 조회 (로그인 성공 후 동기화 로직이 필요하지만 일단 유지)
    override suspend fun getUserProfile(userId: String): Flow<User> {
        return localDataSource.getUserById(userId).map { userEntity ->
            userEntity?.toDomain() ?: getTemporaryUser(userId)
        }
    }

    // (기존 유지)
    override suspend fun updateUserProfile(user: User): Flow<Unit> {
        localDataSource.upsertUser(user.toEntity())
        return flowOf(Unit)
    }

    // (간소화) Firebase는 ID 중복을 createUser 시점에 체크하므로, 여기선 항상 false(중복안됨)로 통과시킴
    override suspend fun checkUserExists(id: String): Boolean {
        return false
    }

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