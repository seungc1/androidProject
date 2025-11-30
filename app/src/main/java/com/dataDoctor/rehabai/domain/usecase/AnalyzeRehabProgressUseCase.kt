// app/src/main/java/com/example/androidproject/domain/usecase/AnalyzeRehabProgressUseCase.kt
package com.dataDoctor.rehabai.domain.usecase

import com.dataDoctor.rehabai.domain.model.ProgressAnalysisResult
import com.dataDoctor.rehabai.domain.repository.RehabSessionRepository
import com.dataDoctor.rehabai.domain.repository.DietSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine // 두 개의 Flow를 합치기 위해
import kotlinx.coroutines.flow.catch // 예외 처리 추가
import javax.inject.Inject

class AnalyzeRehabProgressUseCase @Inject constructor(
    private val rehabSessionRepository: RehabSessionRepository,
    private val dietSessionRepository: DietSessionRepository
) {
    suspend operator fun invoke(userId: String): Flow<ProgressAnalysisResult> {
        // 두 개의 Flow (재활 기록, 식단 기록)를 동시에 수집하여 분석
        return combine(
            rehabSessionRepository.getRehabHistory(userId), // Flow<List<RehabSession>>
            dietSessionRepository.getDietHistory(userId)    // Flow<List<DietSession>>
        ) { rehabSessions, dietSessions -> // 두 Flow의 최신 값이 도착하면 이 람다 실행
            // 1-2. 복합 분석 로직 구현
            val totalRehabSessions = rehabSessions.size
            val totalDietSessions = dietSessions.size

            // RehabSession의 userRating 평균 계산
            val averageRehabRating = if (rehabSessions.isNotEmpty()) {
                rehabSessions.mapNotNull { it.userRating }.average() // userRating이 null이 아닌 경우만 포함
            } else 0.0

            // DietSession의 userSatisfaction 평균 계산 (DietSession에 userSatisfaction 필드 필요)
            val averageDietSatisfaction = if (dietSessions.isNotEmpty()) {
                dietSessions.mapNotNull { it.userSatisfaction }.average() // userSatisfaction이 null이 아닌 경우만 포함
            } else 0.0

            // 분석 결과에 따른 피드백 메시지 생성
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
            // 예외 처리 추가
            .catch { e ->
                // 예외 발생 시 로깅하고, 빈 ProgressAnalysisResult를 반환
                println("AnalyzeRehabProgressUseCase: Error analyzing progress: ${e.message}")
                emit(ProgressAnalysisResult(0, 0, 0.0, 0.0, "진행 상황을 분석할 수 없습니다."))
            }
    }
}