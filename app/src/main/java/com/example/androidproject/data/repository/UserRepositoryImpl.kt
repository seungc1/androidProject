package com.example.androidproject.data.repository

import com.example.androidproject.domain.model.User
import com.example.androidproject.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf // Flow를 반환하기 위해 필요
import javax.inject.Inject

// UserRepository 인터페이스의 실제 구현체
class UserRepositoryImpl @Inject constructor(
    // 나중에 UserLocalDataSource 등을 주입받을 예정
) : UserRepository {

    // 더미 사용자 데이터 (초기 개발용)
    private val dummyUser = User(
        id = "user001",
        name = "김재활",
        gender = "남성",
        age = 30,
        heightCm = 175,
        weightKg = 70.0,
        activityLevel = "활동적",
        fitnessGoal = "근육 증가",
        allergyInfo = listOf("새우", "땅콩"),
        preferredDietType = "일반",
        targetCalories = 2500,
        currentInjuryId = null,
        preferredDietaryTypes = listOf("일반", "저염식"), // (예시 값)
        equipmentAvailable = listOf("덤벨", "요가매트"), // (예시 값)
        currentPainLevel = 2, // (예시 값, 1-10)
        additionalNotes = "특별한 사항 없음", // (예시 값)
    )

    override suspend fun getUserProfile(userId: String): Flow<User> {
        // 실제 구현에서는 로컬 DB 또는 서버 API에서 사용자 프로필을 가져옵니다.
        // 현재는 더미 데이터를 반환합니다.
        return flowOf(dummyUser)
    }

    override suspend fun updateUserProfile(user: User): Flow<Unit> {
        // 실제 구현에서는 로컬 DB 또는 서버 API에 사용자 프로필을 업데이트합니다.
        // 현재는 아무 작업도 하지 않는 Unit을 반환합니다.
        // dummyUser = user // 실제 앱에서는 내부 저장소를 업데이트해야 합니다.
        return flowOf(Unit) // 성공 시 Unit 반환
    }
}