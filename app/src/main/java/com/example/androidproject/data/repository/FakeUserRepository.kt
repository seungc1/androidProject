package com.example.androidproject.data.repository

import com.example.androidproject.domain.model.User
import com.example.androidproject.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Singleton // Hilt 테스트에서 Singleton 바인딩을 위해 필요할 수 있음
import javax.inject.Inject // Hilt 테스트에서 Injectable 클래스로 사용하기 위해 필요할 수 있음

// UserRepository 인터페이스를 가짜로 구현한 클래스
// 테스트 시나리오에 따라 사용자 데이터를 쉽게 설정하고 조회할 수 있습니다.
// @Singleton과 @Inject는 Hilt 테스트 환경에서 이 Fake Repository가 잘 주입되도록 돕습니다.
@Singleton
class FakeUserRepository @Inject constructor() : UserRepository {

    // 테스트 시나리오에 따라 설정할 수 있는 초기 사용자 데이터
    // 초기값은 기본 사용자 정보로 설정
    private val _user = MutableStateFlow(
        User(
            id = "testUser",
            name = "테스트 유저",
            gender = "남성",
            age = 30,
            heightCm = 170,
            weightKg = 65.0,
            activityLevel = "활동적",
            fitnessGoal = "체중 감량",
            allergyInfo = emptyList(), // 기본값은 알레르기 없음
            preferredDietType = "일반",
            targetCalories = 2000,
            currentInjuryId = null
        )
    )

    // 테스트 코드에서 사용자 데이터를 쉽게 변경할 수 있도록 public getter 제공
    var currentUser: User
        get() = _user.value
        set(value) { _user.value = value }

    override suspend fun getUserProfile(userId: String): Flow<User> {
        // 요청된 userId와 현재 설정된 더미 사용자의 id가 일치할 경우에만 반환
        // 실제 테스트에서는 특정 userId에 대한 응답을 더 정교하게 만들 수 있습니다.
        return _user
    }

    override suspend fun updateUserProfile(user: User): Flow<Unit> {
        _user.value = user // 사용자 프로필 업데이트 (인메모리)
        return flowOf(Unit)
    }
}