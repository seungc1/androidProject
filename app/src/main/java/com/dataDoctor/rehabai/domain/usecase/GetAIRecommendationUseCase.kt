// app/src/main/java/com/example/androidproject/domain/usecase/GetAIRecommendationUseCase.kt

package com.dataDoctor.rehabai.domain.usecase

import com.dataDoctor.rehabai.domain.model.AIRecommendationResult
import com.dataDoctor.rehabai.domain.model.Injury
import com.dataDoctor.rehabai.domain.model.RecommendationParams
import com.dataDoctor.rehabai.domain.repository.AIApiRepository
import com.dataDoctor.rehabai.domain.repository.RehabSessionRepository
import com.dataDoctor.rehabai.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.catch
import javax.inject.Inject

/**
 * (★수정★)
 * 사용자의 프로필, 부상 정보, '그리고' '과거 운동 피드백'을 기반으로
 * AI로부터 '적응형' '멀티-데이 루틴'을 가져오는 Use Case
 */
class GetAIRecommendationUseCase @Inject constructor(
    private val aiApiRepository: AIApiRepository,
    private val userRepository: UserRepository,
    private val rehabSessionRepository: RehabSessionRepository
) {

    suspend operator fun invoke(userId: String, injuryInfo: Injury?): Flow<AIRecommendationResult> {

        // 1. UserRepository를 통해 현재 사용자의 최신 프로필 정보(User)를 가져옵니다.
        val currentUser = userRepository.getUserProfile(userId).first()

        // 2. AI가 '학습'할 수 있도록, 사용자의 '모든' '과거 운동 기록'을 가져옵니다.
        val pastSessions = rehabSessionRepository.getRehabHistory(userId).first()

        // 3.  GPT API 요청을 위한 RecommendationParams 객체에 '과거 기록'을 '포함'합니다.
        val recommendationParams = RecommendationParams(
            userId = currentUser.id,
            age = currentUser.age,
            gender = currentUser.gender,
            heightCm = currentUser.heightCm,
            weightKg = currentUser.weightKg,
            activityLevel = currentUser.activityLevel,
            fitnessGoal = currentUser.fitnessGoal,
            dietaryPreferences = currentUser.preferredDietaryTypes,
            allergies = currentUser.allergyInfo,
            equipmentAvailable = currentUser.equipmentAvailable,
            currentPainLevel = currentUser.currentPainLevel,
            injuryArea = injuryInfo?.bodyPart,
            injuryType = injuryInfo?.name,
            injurySeverity = injuryInfo?.severity,
            additionalNotes = currentUser.additionalNotes,
            pastSessions = pastSessions //  AI가 학습할 데이터
        )

        // 4. AIApiRepository를 통해 AI 추천을 요청합니다.
        return aiApiRepository.getAIRehabAndDietRecommendation(recommendationParams)
            .map { aiResult ->
                // 5. AI가 프롬프트에서 직접 필터링/조정을 수행하므로,
                //    AI가 반환한 'scheduledWorkouts'를 그대로 반환합니다.
                AIRecommendationResult(
                    scheduledWorkouts = aiResult.scheduledWorkouts,
                    scheduledDiets = aiResult.scheduledDiets, // (식단은 그대로)
                    overallSummary = aiResult.overallSummary
                )
            }
            // 6. 예외 처리
            .catch { e ->
                println("GetAIRecommendationUseCase: Error fetching AI recommendations: ${e.message}")
                //  새 모델 구조에 맞게 빈 결과 반환
                emit(AIRecommendationResult(emptyList(), emptyList()))
            }
    }
}