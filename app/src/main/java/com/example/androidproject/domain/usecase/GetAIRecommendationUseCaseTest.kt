package com.example.androidproject.domain.usecase

// JUnit Assertions 임포트
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

// Fake Repositories 임포트 (경로 확인 필수)
import com.example.androidproject.data.repository.FakeAIApiRepository
import com.example.androidproject.data.repository.FakeUserRepository

// Domain Models 임포트
import com.example.androidproject.domain.model.AIRecommendationResult
import com.example.androidproject.domain.model.Diet
import com.example.androidproject.domain.model.Exercise
import com.example.androidproject.domain.model.Injury
import com.example.androidproject.domain.model.User

// Coroutines 테스트용
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class GetAIRecommendationUseCaseTest {

    // 테스트 대상 UseCase와 가짜(Fake) Repository 선언
    private lateinit var getAIRecommendationUseCase: GetAIRecommendationUseCase
    private lateinit var fakeAIApiRepository: FakeAIApiRepository
    private lateinit var fakeUserRepository: FakeUserRepository

    @Before
    fun setup() {
        // Hilt가 아닌, 수동으로 가짜 Repository 인스턴스를 생성하여 UseCase에 주입
        fakeAIApiRepository = FakeAIApiRepository()
        fakeUserRepository = FakeUserRepository()
        getAIRecommendationUseCase = GetAIRecommendationUseCase(
            aiApiRepository = fakeAIApiRepository,
            userRepository = fakeUserRepository
        )
    }

    @Test
    fun `allergyFilter_shouldWorkCorrectly`() = runBlocking {
        // Given: 테스트 시나리오 설정
        // ⭐️ FIX 1: fakeUserRepository의 currentUser 속성을 통해 사용자 정보 설정
        fakeUserRepository.currentUser = fakeUserRepository.currentUser.copy(
            allergyInfo = listOf("새우", "땅콩")
        )

        // AI가 알레르기 유발 식품(새우, 땅콩)을 포함하여 추천했다고 가정
        fakeAIApiRepository.testResult = AIRecommendationResult(
            emptyList(),
            listOf(
                Diet("d1", "점심", "새우 볶음밥", 300.0, "g", 500, 30.0, 15.0, 50.0, listOf("쌀", "새우", "야채"), null, null),
                Diet("d2", "아침", "견과류 시리얼", 200.0, "g", 300, 10.0, 10.0, 40.0, listOf("시리얼", "우유", "땅콩"), null, null),
                Diet("d3", "저녁", "닭가슴살 샐러드", 250.0, "g", 350, 40.0, 10.0, 20.0, listOf("닭가슴살", "채소"), null, null)
            )
        )

        // When: UseCase 실행
        val result = getAIRecommendationUseCase("testUser", null).first()

        // Then: 검증
        assertEquals("알레르기 식품이 필터링되어 1개만 남아야 합니다.", 1, result.recommendedDiets.size)
        assertEquals("닭가슴살 샐러드", result.recommendedDiets[0].foodName)
    }

    @Test
    fun `injuryAdjustment_shouldWorkCorrectly`() = runBlocking {
        // Given: 테스트 시나리오 설정
        // ⭐️ FIX 1: fakeUserRepository의 currentUser 속성을 통해 사용자 정보 설정
        fakeUserRepository.currentUser = fakeUserRepository.currentUser.copy(allergyInfo = emptyList())

        // AI가 '고급' 어깨 운동을 추천했다고 가정
        fakeAIApiRepository.testResult = AIRecommendationResult(
            listOf(
                Exercise("e1", "어깨 프레스", "어깨 강화", "어깨", "고급", "", null, null),
                Exercise("e2", "런지", "하체 운동", "하체", "중급", "", null, null)
            ),
            emptyList()
        )

        // ⭐️ FIX 2: Injury 생성자를 domain/model에 정의된 5개 인자에 맞게 수정 ⭐️
        val severeShoulderInjury = Injury(
            id = "i1",
            name = "어깨 염좌",
            bodyPart = "어깨",
            severity = "심각", // UseCase의 로직이 '심각'을 인지해야 함
            description = "테스트용 심각한 어깨 부상"
        )

        // When: UseCase 실행 (심각한 어깨 부상 정보 전달)
        val result = getAIRecommendationUseCase("testUser", severeShoulderInjury).first()

        // Then: 검증
        assertEquals(2, result.recommendedExercises.size)

        // ⭐️ FIX 3 & 4: Nullable 타입 처리 ⭐️
        // 1. 어깨 운동이 존재하는지 먼저 확인
        val shoulderExercise = result.recommendedExercises.find { it.bodyPart == "어깨" }
        assertNotNull("어깨 운동이 추천 목록에 포함되어야 합니다.", shoulderExercise)

        // 2. Null이 아님을 확인했으므로, !!. 연산자(non-null assertion)를 사용하여 속성에 안전하게 접근
        assertEquals("심각한 부상으로 난이도가 '초급'으로 하향 조정되어야 합니다.", "초급", shoulderExercise!!.difficulty)
        assertTrue(
            "난이도 조정 사유가 포함되어야 합니다.",
            shoulderExercise.precautions?.contains("어깨 부상으로 인한 난이도 하향 및 특별 주의 필요.") == true
        )

        // 3. 부상과 관련 없는 하체 운동은 그대로인지 확인
        val legExercise = result.recommendedExercises.find { it.bodyPart == "하체" }
        assertNotNull("하체 운동이 추천 목록에 포함되어야 합니다.", legExercise)
        assertEquals("하체 운동은 '중급' 난이도가 유지되어야 합니다.", "중급", legExercise!!.difficulty)
    }
}