// app/src/main/java/com/example/androidproject/domain/usecase/GetRecommendedRehabUseCase.kt
package com.example.androidproject.domain.usecase

import com.example.androidproject.domain.model.Exercise
import com.example.androidproject.domain.model.Injury
import com.example.androidproject.domain.repository.RehabRepository
import javax.inject.Inject

// 사용자의 부상 정보를 기반으로 맞춤형 재활 운동을 추천하는 Use Case
class GetRecommendedRehabUseCase @Inject constructor(
    private val repository: RehabRepository // Hilt를 통해 RehabRepository 의존성 주입
) {
    // 핵심 비즈니스 로직 실행 함수
    suspend operator fun invoke(injury: Injury): List<Exercise> {
        // 1. Repository를 통해 기본적인 추천 운동 목록을 가져옵니다. (AI/규칙 기반 초기 추천)
        val allRecommended = repository.getRecommendedExercises(injury)

        // 2. 사용자의 통증 부위를 고려하여 부담을 줄 수 있는 운동을 제외하는 로직을 추가합니다.
        //    (이 로직은 초기에는 간단한 규칙 기반으로 시작하며, 나중에 AI 모델로 고도화됩니다.)
        val filteredExercises = allRecommended.filter { exercise ->
            // 운동의 riskBodyParts에 사용자의 부상 부위가 포함되어 있고, 통증 수준이 높으면 제외
            !(exercise.riskBodyParts.contains(injury.bodyPart) && injury.painLevel >= 5)
        }

        return filteredExercises
    }
}