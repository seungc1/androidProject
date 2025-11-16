package com.example.androidproject.data.repository

// ✅ [추가] LocalDataSource와 Mapper를 import
import com.example.androidproject.data.local.datasource.LocalDataSource
import com.example.androidproject.data.mapper.toDomain
import com.example.androidproject.data.mapper.toEntity
import com.example.androidproject.domain.model.User
import com.example.androidproject.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map // ✅ [추가] Flow.map을 import
import javax.inject.Inject

// ✅ [수정] Hilt가 LocalDataSource를 주입하도록 생성자 변경
class UserRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDataSource // ✅ [추가]
) : UserRepository {

    // ❌ --- 'dummyUser' 관련 코드 '전부 삭제' ---
    // private val dummyUser = User(...)
    // ❌ ------------------------------------

    override suspend fun getUserProfile(userId: String): Flow<User> {
        // ✅ [수정] LocalDataSource에서 데이터를 조회하고 'toDomain'으로 번역
        return localDataSource.getUserById(userId).map { userEntity ->
            // DB에 유저가 없으면 '임시' 유저를 반환 (혹은 오류 처리)
            userEntity?.toDomain() ?: getTemporaryUser()
        }
    }

    override suspend fun updateUserProfile(user: User): Flow<Unit> {
        // ✅ [수정] Domain 모델(User)을 'toEntity'로 번역하여 DB에 저장
        localDataSource.upsertUser(user.toEntity())
        return flowOf(Unit) // 성공 시 Unit 반환
    }

    // ✅ [추가] getUserProfile이 null일 경우를 대비한 임시 유저
    private fun getTemporaryUser(): User {
        return User(
            id = "tempUser", name = "사용자", gender = "", age = 0,
            heightCm = 0, weightKg = 0.0, activityLevel = "",
            fitnessGoal = "", allergyInfo = emptyList(),
            preferredDietType = "", targetCalories = 0,
            currentInjuryId = null, preferredDietaryTypes = emptyList(),
            equipmentAvailable = emptyList(), currentPainLevel = 0, additionalNotes = null
        )
    }
}