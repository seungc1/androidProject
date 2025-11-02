package com.example.androidproject.domain.usecase

import com.example.androidproject.domain.model.AIRecommendationResult
import com.example.androidproject.domain.model.Injury
import com.example.androidproject.domain.model.User
import com.example.androidproject.domain.repository.AIApiRepository
import com.example.androidproject.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map // Flow의 데이터를 변환하기 위해 필요
import javax.inject.Inject

// 사용자의 부상 및 건강 정보를 기반으로 AI로부터 운동 및 식단 추천을 가져오는 Use Case
class GetAIRecommendationUseCase @Inject constructor(
    private val aiApiRepository: AIApiRepository, // AI API와의 통신 담당
    private val userRepository: UserRepository // 사용자 프로필 정보를 가져오기 위함
) {
    // 핵심 비즈니스 로직 실행 함수
    // userId와 injuryInfo를 받아 AI로부터 추천 결과를 Flow로 반환
    suspend operator fun invoke(userId: String, injuryInfo: Injury?): Flow<AIRecommendationResult> {
        // 1. UserRepository를 통해 현재 사용자의 최신 프로필 정보(User)를 가져옵니다.
        //    first()는 Flow에서 첫 번째 값을 가져오고 Flow를 종료합니다.
        val currentUser = userRepository.getUserProfile(userId).first()

        // 2. 가져온 User 정보와 injuryInfo를 바탕으로 AIApiRepository에 AI 추천을 요청합니다.
        return aiApiRepository.getAIRehabAndDietRecommendation(currentUser, injuryInfo)
            .map { aiResult ->
                // 3. AI 응답 (AIRecommendationResult)을 받은 후, 추가적인 비즈니스 로직을 적용합니다.
                //    (Domain Layer의 핵심 로직!)

                // 3-1. 알레르기 필터링 (식단)
                val filteredDiets = aiResult.recommendedDiets.filter { diet ->
                    // 사용자의 알레르기 정보에 해당 식단의 재료가 포함되어 있는지 확인
                    !currentUser.allergyInfo.any { allergy ->
                        diet.ingredients.any { ingredient ->
                            ingredient.contains(allergy, ignoreCase = true) // 대소문자 무시 비교
                        }
                    }
                }

                // 3-2. 부상에 따른 운동 제약 조건 확인 및 조정 (운동)
                val adjustedExercises = aiResult.recommendedExercises.map { exercise ->
                    if (injuryInfo != null &&
                        exercise.bodyPart.equals(injuryInfo.bodyPart, ignoreCase = true) && // 운동 부위가 부상 부위와 같고
                        injuryInfo.severity.equals("심각", ignoreCase = true) && // 부상 심각도가 높다면
                        exercise.difficulty.equals("고급", ignoreCase = true) // 고급 운동은 피하도록 조정 (예시 로직)
                    ) {
                        // 심각한 부상 부위의 고급 운동은 중급으로 강제 조정하거나, 추천 이유를 추가
                        exercise.copy(
                            difficulty = "중급",
                            aiRecommendationReason = (exercise.aiRecommendationReason ?: "") +
                                    "\n(주의: ${injuryInfo.bodyPart} 부상이 심각하여 난이도가 하향 조정되었습니다.)"
                        )
                    } else {
                        exercise
                    }
                }

                // 3-3. 선호 식단 유형 반영 (예시 로직 - 더 복잡하게 구현 가능)
                // 현재는 필터링보다 AI 프롬프트에서 반영하는 것이 좋지만, 후처리 예시
                val finalDiets = filteredDiets.filter { diet ->
                    when (currentUser.preferredDietType.lowercase()) {
                        "비건" -> !diet.ingredients.any { it.contains("고기", ignoreCase = true) || it.contains("유제품", ignoreCase = true) || it.contains("계란", ignoreCase = true) }
                        "저탄수화물" -> diet.carbs <= 50 // 예시: 탄수화물 50g 이하
                        else -> true // 기타 유형은 필터링 없음
                    }
                }


                // 최종적으로 필터링 및 조정된 결과 반환
                AIRecommendationResult(
                    recommendedExercises = adjustedExercises,
                    recommendedDiets = finalDiets
                )
            }
    }
}