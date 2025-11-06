package com.example.androidproject.domain.usecase

import com.example.androidproject.data.repository.FakeDietSessionRepository
import com.example.androidproject.data.repository.FakeRehabSessionRepository
import com.example.androidproject.domain.model.DietSession
import com.example.androidproject.domain.model.RehabSession
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

import java.util.Date

class AnalyzeRehabProgressUseCaseTest {

    private lateinit var analyzeRehabProgressUseCase: AnalyzeRehabProgressUseCase
    private lateinit var fakeRehabSessionRepository: FakeRehabSessionRepository
    private lateinit var fakeDietSessionRepository: FakeDietSessionRepository

    private val TEST_USER_ID = "testUser123"

    @Before
    fun setup() {
        fakeRehabSessionRepository = FakeRehabSessionRepository()
        fakeDietSessionRepository = FakeDietSessionRepository()
        analyzeRehabProgressUseCase = AnalyzeRehabProgressUseCase(
            rehabSessionRepository = fakeRehabSessionRepository,
            dietSessionRepository = fakeDietSessionRepository
        )
    }

    @Test
    fun `initialAnalysis_shouldReturnNoActivityMessageWhenNoSessions`() = runBlocking {
        // Given: 아무런 세션도 기록되지 않은 상태

        // When: UseCase 실행
        val result = analyzeRehabProgressUseCase(TEST_USER_ID).first()

        // Then: "아직 기록된 활동이 없습니다" 메시지 반환 확인
        assertEquals(0, result.totalRehabSessions)
        assertEquals(0, result.totalDietSessions)
        assertEquals(0.0, result.averageRehabRating, 0.0)
        assertEquals(0.0, result.averageDietSatisfaction, 0.0)
        assertEquals("아직 기록된 활동이 없습니다. 첫 기록을 시작해 보세요!", result.feedbackMessage)
    }

    @Test
    fun `analysis_shouldCorrectlyCalculateAveragesAndProvideFeedback`() = runBlocking {
        // Given: 여러 운동 및 식단 세션 기록
        fakeRehabSessionRepository.addTestSession(
            RehabSession(
                id = "r1", userId = TEST_USER_ID, exerciseId = "e1", dateTime = Date(),
                durationMinutes = 30, sets = 3, reps = 10,
                userRating = 5, notes = "굿" // ⭐ 명명 인자 적용
            )
        )
        fakeRehabSessionRepository.addTestSession(
            RehabSession(
                id = "r2", userId = TEST_USER_ID, exerciseId = "e2", dateTime = Date(),
                durationMinutes = 45, sets = 4, reps = 8,
                userRating = 4, notes = "보통" // ⭐ 명명 인자 적용
            )
        )
        fakeDietSessionRepository.addTestSession(
            DietSession(
                id = "d1", userId = TEST_USER_ID, dietId = "diet1", dateTime = Date(),
                actualQuantity = 1.0,
                actualUnit = "Portion", // ⭐ 명명 인자 적용 (actualUnit 추가)
                userSatisfaction = 5,
                notes = "맛있음" // ⭐ 명명 인자 적용 (notes 추가)
            )
        )
        fakeDietSessionRepository.addTestSession(
            DietSession(
                id = "d2", userId = TEST_USER_ID, dietId = "diet2", dateTime = Date(),
                actualQuantity = 0.5,
                actualUnit = "개", // ⭐ 명명 인자 적용 (actualUnit 추가)
                userSatisfaction = 4,
                notes = "괜찮음" // ⭐ 명명 인자 적용 (notes 추가)
            )
        )

        // When: UseCase 실행
        val result = analyzeRehabProgressUseCase(TEST_USER_ID).first()

        // Then: 예상되는 분석 결과 검증
        assertEquals(2, result.totalRehabSessions)
        assertEquals(2, result.totalDietSessions)
        assertEquals(4.5, result.averageRehabRating, 0.0)
        assertEquals(4.5, result.averageDietSatisfaction, 0.0)
        assertEquals("운동과 식단 모두 훌륭합니다! 꾸준히 유지해 주세요.", result.feedbackMessage)
    }

    @Test
    fun `analysis_shouldIdentifyLowRehabSatisfaction`() = runBlocking {
        // Given: 낮은 운동 만족도
        fakeRehabSessionRepository.addTestSession(
            RehabSession(
                id = "r1", userId = TEST_USER_ID, exerciseId = "e1", dateTime = Date(),
                durationMinutes = 30, sets = 3, reps = 10,
                userRating = 2, notes = "힘듦" // ⭐ 명명 인자 적용
            )
        )
        fakeDietSessionRepository.addTestSession(
            DietSession(
                id = "d1", userId = TEST_USER_ID, dietId = "diet1", dateTime = Date(),
                actualQuantity = 1.0,
                actualUnit = "Portion", // ⭐ 명명 인자 적용
                userSatisfaction = 5,
                notes = "맛있음" // ⭐ 명명 인자 적용
            )
        )

        // When: UseCase 실행
        val result = analyzeRehabProgressUseCase(TEST_USER_ID).first()

        // Then: 낮은 운동 만족도 메시지 확인
        assertEquals(2.0, result.averageRehabRating, 0.0)
        assertEquals(1, result.totalRehabSessions)
        assertEquals(5.0, result.averageDietSatisfaction, 0.0)
        assertEquals("운동 만족도가 낮은 편입니다. 운동 방법을 점검해 보세요.", result.feedbackMessage)
    }

    @Test
    fun `analysis_shouldHandleEmptyRatingsCorrectly`() = runBlocking {
        // Given: userRating이 null인 세션
        fakeRehabSessionRepository.addTestSession(
            RehabSession(
                id = "r1", userId = TEST_USER_ID, exerciseId = "e1", dateTime = Date(),
                durationMinutes = 30, sets = 3, reps = 10,
                userRating = null, notes = "평가없음" // ⭐ 명명 인자 적용
            )
        )
        fakeRehabSessionRepository.addTestSession(
            RehabSession(
                id = "r2", userId = TEST_USER_ID, exerciseId = "e2", dateTime = Date(),
                durationMinutes = 45, sets = 4, reps = 8,
                userRating = 4, notes = "괜찮음" // ⭐ 명명 인자 적용
            )
        )
        fakeDietSessionRepository.addTestSession(
            DietSession(
                id = "d1", userId = TEST_USER_ID, dietId = "diet1", dateTime = Date(),
                actualQuantity = 1.0,
                actualUnit = "g", // ⭐ 명명 인자 적용
                userSatisfaction = null, // ⭐ userSatisfaction을 null로 설정
                notes = "평가없음" // ⭐ 명명 인자 적용
            )
        )
        fakeDietSessionRepository.addTestSession(
            DietSession(
                id = "d2", userId = TEST_USER_ID, dietId = "diet2", dateTime = Date(),
                actualQuantity = 0.5,
                actualUnit = "개", // ⭐ 명명 인자 적용
                userSatisfaction = 5, // ⭐ userSatisfaction을 5로 설정
                notes = "괜찮음" // ⭐ 명명 인자 적용
            )
        )

        // When: UseCase 실행
        val result = analyzeRehabProgressUseCase(TEST_USER_ID).first()

        // Then: null이 아닌 값들로만 평균 계산
        assertEquals(2, result.totalRehabSessions)
        assertEquals(4.0, result.averageRehabRating, 0.0) // null인 r1은 평균에서 제외 (4 / 1)
        assertEquals(5.0, result.averageDietSatisfaction, 0.0) // null인 d1은 평균에서 제외 (5 / 1)
    }
}