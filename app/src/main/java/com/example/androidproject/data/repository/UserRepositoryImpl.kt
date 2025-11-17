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
import kotlinx.coroutines.flow.first

// ✅ [수정] Hilt가 LocalDataSource를 주입하도록 생성자 변경
class UserRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDataSource // ✅ [추가]
) : UserRepository {

    // ✅ [추가] 로그인 구현
    override suspend fun login(id: String, password: String): Boolean {
        // 1. DB에서 해당 ID의 유저를 찾아봅니다.
        val userEntity = localDataSource.getUserById(id).first()

        return if (userEntity == null) {
            // 2-A. 유저가 없으면 -> '자동 회원가입' 시키고 로그인 성공 처리
            // (편의를 위해, 로그인 시도한 ID/PW로 새 유저를 만듭니다)
            val newUser = getTemporaryUser().copy(id = id, password = password)
            localDataSource.upsertUser(newUser.toEntity())
            true // 로그인(회원가입) 성공
        } else {
            // 2-B. 유저가 있으면 -> '비밀번호 확인'
            userEntity.password == password
        }
    }

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
            id = "tempUser", password = "", name = "사용자", gender = "", age = 0,
            heightCm = 0, weightKg = 0.0, activityLevel = "",
            fitnessGoal = "", allergyInfo = emptyList(),
            preferredDietType = "", targetCalories = 0,
            currentInjuryId = null, preferredDietaryTypes = emptyList(),
            equipmentAvailable = emptyList(), currentPainLevel = 0, additionalNotes = null
        )
    }
}