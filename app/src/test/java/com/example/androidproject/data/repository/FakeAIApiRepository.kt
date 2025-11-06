// app/src/test/java/com/example/androidproject/data/repository/FakeAIApiRepository.kt
package com.example.androidproject.data.repository

import com.example.androidproject.domain.model.AIRecommendationResult
import com.example.androidproject.domain.model.Diet
import com.example.androidproject.domain.model.Exercise
import com.example.androidproject.domain.model.Injury
import com.example.androidproject.domain.model.User
import com.example.androidproject.domain.repository.AIApiRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

// 테스트를 위해 AIApiRepository 인터페이스를 가짜로 구현한 클래스
class FakeAIApiRepository : AIApiRepository {

    // 테스트 시나리오에 따라 반환할 결과를 설정할 수 있는 변수
    var shouldReturnError = false
    var testResult: AIRecommendationResult? = null

    // 기본 더미 결과
    private val defaultResult = AIRecommendationResult(
        recommendedExercises = listOf(
            Exercise("e1", "기본 스트레칭", "팔다리 스트레칭", "전신", "초급", "", null, "기본 추천"),
            Exercise("e2", "가벼운 걷기", "유산소 운동", "하체", "초급", "", null, "기본 추천")
        ),
        recommendedDiets = listOf(
            Diet("d1", "아침", "사과", 1.0, "개", 80, 0.5, 0.5, 20.0, listOf("사과"), null, "기본 추천")
        )
    )

    override suspend fun getAIRehabAndDietRecommendation(
        userInfo: User,
        injuryInfo: Injury?
    ): Flow<AIRecommendationResult> {
        if (shouldReturnError) {
            // TODO: 실제 오류를 발생시키는 Flow로 변경 가능 (예: flow { throw Exception("API Error") })
            return flowOf(AIRecommendationResult(emptyList(), emptyList())) // 오류 시 빈 결과 반환
        }
        return flowOf(testResult ?: defaultResult)
    }
}