// app/src/main/java/com/example/androidproject/domain/usecase/GetAIRecommendationUseCase.kt

package com.example.androidproject.domain.usecase

import com.example.androidproject.domain.model.AIRecommendationResult
import com.example.androidproject.domain.model.Injury
import com.example.androidproject.domain.model.RecommendationParams // 추가
import com.example.androidproject.domain.model.User
import com.example.androidproject.domain.repository.AIApiRepository
import com.example.androidproject.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.catch
// flowOf는 emit으로 대체되었으므로 제거 가능

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
        val currentUser = userRepository.getUserProfile(userId).first() // first()는 Flow에서 첫 번째 값을 가져오고 Flow를 종료합니다.

        // 2. GPT API 요청을 위한 RecommendationParams 객체 생성
        val recommendationParams = RecommendationParams(
            userId = currentUser.id,
            age = currentUser.age,
            gender = currentUser.gender,
            heightCm = currentUser.heightCm,
            weightKg = currentUser.weightKg,
            activityLevel = currentUser.activityLevel,
            fitnessGoal = currentUser.fitnessGoal,
            dietaryPreferences = currentUser.preferredDietaryTypes, // User 모델에 preferredDietaryTypes가 있다고 가정
            allergies = currentUser.allergyInfo,
            equipmentAvailable = currentUser.equipmentAvailable, // User 모델에 equipmentAvailable이 있다고 가정
            currentPainLevel = currentUser.currentPainLevel, // User 모델에 currentPainLevel이 있다고 가정
            injuryArea = injuryInfo?.bodyPart,
            injuryType = injuryInfo?.name,
            injurySeverity = injuryInfo?.severity,
            additionalNotes = currentUser.additionalNotes // User 모델에 additionalNotes가 있다고 가정
        )

        // 3. AIApiRepository를 통해 AI 추천을 요청합니다.
        return aiApiRepository.getAIRehabAndDietRecommendation(recommendationParams)
            .map { aiResult ->
                // 4. AI 응답 (AIRecommendationResult)을 받은 후, 추가적인 비즈니스 로직을 적용합니다.
                //    (Domain Layer의 핵심 로직!)

                // 4-1. 알레르기 필터링 (식단)
                val filteredDietsByAllergy = aiResult.recommendedDiets.filter { diet ->
                    // 식단의 재료 중 사용자의 알레르기 정보에 포함된 것이 없는지 확인
                    !currentUser.allergyInfo.any { allergy -> // 사용자의 각 알레르기에 대해
                        diet.ingredients.any { ingredient -> // 식단의 각 재료가
                            ingredient.contains(allergy, ignoreCase = true) // 알레르기 물질을 포함하는지 검사
                        }
                    }
                }

                // 4-2. 부상에 따른 운동 제약 조건 확인 및 조정 (운동)
                val adjustedExercises = aiResult.recommendedExercises.map { exercise ->
                    if (injuryInfo != null && // 부상 정보가 있고
                        exercise.bodyPart.equals(injuryInfo.bodyPart, ignoreCase = true) && // 운동 부위가 부상 부위와 같고
                        injuryInfo.severity.equals("심각", ignoreCase = true) // 부상 심각도가 '심각'이라면
                    ) {
                        // 심각한 부상 부위와 관련된 운동에 대한 조정
                        // '고급' 운동은 '중급'으로 하향 조정하고, 주의사항 추가
                        val newDifficulty = if (exercise.difficulty.equals("고급", ignoreCase = true)) "중급" else exercise.difficulty
                        exercise.copy(
                            difficulty = newDifficulty,
                            aiRecommendationReason = (exercise.aiRecommendationReason ?: "") +
                                    "\n(주의: ${injuryInfo.bodyPart} 부상이 심각하여 난이도가 하향 조정되었습니다.)"
                        )
                    } else if (injuryInfo != null && // 부상 정보가 있고
                        exercise.bodyPart.equals(injuryInfo.bodyPart, ignoreCase = true) && // 운동 부위가 부상 부위와 같고
                        injuryInfo.severity.equals("보통", ignoreCase = true) // 부상 심각도가 '보통'이라면
                    ) {
                        // 보통 부상 부위와 관련된 운동에 대한 조정
                        // '고급' 운동은 '중급'으로 하향 조정하고, 주의사항 추가 (심각과 동일하게 처리하거나 다르게 설정 가능)
                        val newDifficulty = if (exercise.difficulty.equals("고급", ignoreCase = true)) "중급" else exercise.difficulty
                        exercise.copy(
                            difficulty = newDifficulty,
                            aiRecommendationReason = (exercise.aiRecommendationReason ?: "") +
                                    "\n(주의: ${injuryInfo.bodyPart} 부상으로 주의가 필요합니다.)"
                        )
                    }
                    else {
                        exercise // 그 외의 경우는 운동 그대로 반환
                    }
                }

                // 4-3. 선호 식단 유형 반영 (필터링)
                val finalDiets = filteredDietsByAllergy.filter { diet ->
                    when (currentUser.preferredDietType.lowercase()) { // User 모델에 preferredDietType이 있다고 가정
                        "비건" -> !diet.ingredients.any { it.contains("고기", ignoreCase = true) || it.contains("유제품", ignoreCase = true) || it.contains("계란", ignoreCase = true) }
                        "저탄수화물" -> diet.carbs != null && diet.carbs <= 50.0 // null 체크 추가
                        // "케토" -> ... (다른 식단 유형 추가 가능)
                        else -> true // 기타 유형은 추가 필터링 없음
                    }
                }

                // 최종적으로 필터링 및 조정된 결과 반환
                AIRecommendationResult(
                    recommendedExercises = adjustedExercises,
                    recommendedDiets = finalDiets,
                    overallSummary = aiResult.overallSummary // GPT가 생성한 요약도 포함
                )
            }
            // 5. 네트워크 또는 API 호출 중 발생할 수 있는 예외 처리
            .catch { e ->
                // 실제 앱에서는 로깅 시스템에 오류를 기록하고 (예: Firebase Crashlytics)
                // 사용자에게는 기본값을 제공하거나, 오류 메시지를 전달할 수 있습니다.
                // 여기서는 빈 추천 결과를 반환합니다.
                // TODO: Presentation Layer에서 이 오류를 UI에 표시할 수 있도록 별도 처리 필요
                println("GetAIRecommendationUseCase: Error fetching AI recommendations: ${e.message}")
                emit(AIRecommendationResult(emptyList(), emptyList())) // 비어있는 Flow를 반환하거나 기본값 제공
            }
    }
}