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
        assertEquals(0.0, result.averageRehabRating, 0.0) // Double 비교는 오차 범위 허용
        assertEquals(0.0, result.averageDietSatisfaction, 0.0)
        assertEquals("아직 기록된 활동이 없습니다. 첫 기록을 시작해 보세요!", result.feedbackMessage)
    }

    @Test
    fun `analysis_shouldCorrectlyCalculateAveragesAndProvideFeedback`() = runBlocking {
        // Given: 여러 운동 및 식단 세션 기록
        fakeRehabSessionRepository.addTestSession(
            // ⭐ LocalDateTime.now() 대신 Date() 사용
            // ⭐ userRating은 Int? 타입이므로 정수(5, 4)로 직접 입력
            RehabSession("r1", TEST_USER_ID, "e1", Date(), 30, 3, 10, 5, "굿")
        )
        fakeRehabSessionRepository.addTestSession(
            RehabSession("r2", TEST_USER_ID, "e2", Date(), 45, 4, 8, 4, "보통")
        )
        fakeDietSessionRepository.addTestSession(
            // ⭐ LocalDateTime.now() 대신 Date() 사용
            // ⭐ userSatisfaction은 Int? 타입이므로 정수(5, 4)로 직접 입력
            DietSession("d1", TEST_USER_ID, "diet1", Date(), 1.0, 5, "맛있음")
        )
        fakeDietSessionRepository.addTestSession(
            DietSession("d2", TEST_USER_ID, "diet2", Date(), 0.5, 4, "괜찮음")
        )

        // When: UseCase 실행
        val result = analyzeRehabProgressUseCase(TEST_USER_ID).first()

        // Then: 예상되는 분석 결과 검증
        assertEquals(2, result.totalRehabSessions)
        assertEquals(2, result.totalDietSessions)
        assertEquals(4.5, result.averageRehabRating, 0.0) // (5+4)/2 = 4.5
        assertEquals(4.5, result.averageDietSatisfaction, 0.0) // (5+4)/2 = 4.5
        assertEquals("운동과 식단 모두 훌륭합니다! 꾸준히 유지해 주세요.", result.feedbackMessage)
    }

    @Test
    fun `analysis_shouldIdentifyLowRehabSatisfaction`() = runBlocking {
        // Given: 낮은 운동 만족도
        fakeRehabSessionRepository.addTestSession(
            // ⭐ LocalDateTime.now() 대신 Date() 사용
            RehabSession("r1", TEST_USER_ID, "e1", Date(), 30, 3, 10, 2, "힘듦")
        )
        fakeDietSessionRepository.addTestSession(
            // ⭐ LocalDateTime.now() 대신 Date() 사용
            DietSession("d1", TEST_USER_ID, "diet1", Date(), 1.0, 5, "맛있음")
        )

        // When: UseCase 실행
        val result = analyzeRehabProgressUseCase(TEST_USER_ID).first()

        // Then: 낮은 운동 만족도 메시지 확인
        assertEquals(2.0, result.averageRehabRating, 0.0)
        assertEquals("운동 만족도가 낮은 편입니다. 운동 방법을 점검해 보세요.", result.feedbackMessage)
    }

    @Test
    fun `analysis_shouldHandleEmptyRatingsCorrectly`() = runBlocking {
        // Given: userRating이 null인 세션
        fakeRehabSessionRepository.addTestSession(
            // ⭐ LocalDateTime.now() 대신 Date() 사용
            // ⭐ userRating을 null로 전달
            RehabSession("r1", TEST_USER_ID, "e1", Date(), 30, 3, 10, null, "평가없음")
        )
        fakeRehabSessionRepository.addTestSession(
            // ⭐ LocalDateTime.now() 대신 Date() 사용
            RehabSession("r2", TEST_USER_ID, "e2", Date(), 45, 4, 8, 4, "괜찮음")
        )

        // When: UseCase 실행
        val result = analyzeRehabProgressUseCase(TEST_USER_ID).first()

        // Then: null이 아닌 값들로만 평균 계산
        assertEquals(2, result.totalRehabSessions)
        assertEquals(4.0, result.averageRehabRating, 0.0) // null인 r1은 평균에서 제외
    }
}