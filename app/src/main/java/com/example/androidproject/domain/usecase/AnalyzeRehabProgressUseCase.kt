// app/src/main/java/com/example/androidproject/domain/usecase/AnalyzeRehabProgressUseCase.kt
package com.example.androidproject.domain.usecase

import com.example.androidproject.domain.model.ProgressAnalysisResult // 새로 만든 모델 import
import com.example.androidproject.domain.repository.RehabSessionRepository
import com.example.androidproject.domain.repository.DietSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.combine // 두 개의 Flow를 합치기 위해
import javax.inject.Inject

class AnalyzeRehabProgressUseCase @Inject constructor(
    private val rehabSessionRepository: RehabSessionRepository,
    private val dietSessionRepository: DietSessionRepository
) {
    suspend operator fun invoke(userId: String): Flow<ProgressAnalysisResult> {
        // 두 개의 Flow (재활 기록, 식단 기록)를 동시에 수집하여 분석
        return combine(
            rehabSessionRepository.getRehabHistory(userId),
            dietSessionRepository.getDietHistory(userId)
        ) { rehabSessions, dietSessions ->
            // ✨ 2-1. 복합 분석 로직 구현 시작 ✨
            val totalRehabSessions = rehabSessions.size
            val totalDietSessions = dietSessions.size

            val averageRehabRating = if (rehabSessions.isNotEmpty()) {
                rehabSessions.mapNotNull { it.userRating }.average()
            } else 0.0

            val averageDietSatisfaction = if (dietSessions.isNotEmpty()) {
                dietSessions.mapNotNull { it.userSatisfaction }.average()
            } else 0.0

            val feedbackMessage = when {
                totalRehabSessions == 0 && totalDietSessions == 0 -> "아직 기록된 활동이 없습니다. 첫 기록을 시작해 보세요!"
                averageRehabRating >= 4.0 && averageDietSatisfaction >= 4.0 -> "운동과 식단 모두 훌륭합니다! 꾸준히 유지해 주세요."
                averageRehabRating < 3.0 -> "운동 만족도가 낮은 편입니다. 운동 방법을 점검해 보세요."
                averageDietSatisfaction < 3.0 -> "식단 만족도가 낮은 편입니다. 식단 구성을 다시 확인해 보세요."
                else -> "현재까지 좋은 진행을 보이고 있습니다!"
            }

            ProgressAnalysisResult(
                totalRehabSessions = totalRehabSessions,
                totalDietSessions = totalDietSessions,
                averageRehabRating = averageRehabRating,
                averageDietSatisfaction = averageDietSatisfaction,
                feedbackMessage = feedbackMessage
            )
        }
    }
}