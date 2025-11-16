package com.example.androidproject.domain.usecase

import com.example.androidproject.domain.model.AIAnalysisResult
import com.example.androidproject.domain.model.RehabData
import com.example.androidproject.domain.model.User
import com.example.androidproject.domain.repository.AIApiRepository
import com.example.androidproject.domain.repository.DietSessionRepository
import com.example.androidproject.domain.repository.RehabSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

/**
 * 지난 7일간의 기록을 수집하여 AI에게 분석을 요청하는 Use Case
 */
class GetWeeklyAnalysisUseCase @Inject constructor(
    private val rehabSessionRepository: RehabSessionRepository,
    private val dietSessionRepository: DietSessionRepository,
    private val aiApiRepository: AIApiRepository
    // (UserRepository가 필요하면 주입)
) {
    suspend operator fun invoke(user: User): Flow<AIAnalysisResult> {
        // 1. 7일 전 날짜 계산
        val calendar = Calendar.getInstance()
        val endDate = calendar.time // 오늘
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val startDate = calendar.time // 7일 전

        // 2. 지난 7일간의 재활 및 식단 기록을 각 Repository에서 가져오기
        val rehabSessions = rehabSessionRepository.getRehabSessionsBetween(user.id, startDate, endDate).first()
        val dietSessions = dietSessionRepository.getDietSessionsBetween(user.id, startDate, endDate).first()

        // 3. AI에게 전달할 RehabData 객체 생성
        val rehabData = RehabData(
            userId = user.id,
            userProfile = user,
            pastRehabSessions = rehabSessions,
            pastDietSessions = dietSessions,
            currentPainLevel = user.currentPainLevel
        )

        // 4. AI Repository에 분석 요청
        return aiApiRepository.analyzeRehabProgress(rehabData)
    }
}