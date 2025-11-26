package com.example.androidproject.domain.usecase

import com.example.androidproject.domain.model.DietSession
import com.example.androidproject.domain.model.RehabSession
import com.example.androidproject.domain.repository.DietSessionRepository
import com.example.androidproject.domain.repository.RehabSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

/**
 * 특정 날짜(하루)의 운동 및 식단 기록을 모두 가져오는 Use Case
 */
class GetDailyHistoryUseCase @Inject constructor(
    private val rehabSessionRepository: RehabSessionRepository,
    private val dietSessionRepository: DietSessionRepository
) {
    suspend operator fun invoke(userId: String, date: Date): Flow<Pair<List<RehabSession>, List<DietSession>>> {
        // 1. 선택된 날짜의 시작(00:00:00)과 끝(23:59:59) 시간 계산
        val calendar = Calendar.getInstance().apply { time = date }

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startDate = calendar.time

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endDate = calendar.time

        // 2. 두 Repository에서 해당 기간의 데이터를 Flow로 가져옴
        val rehabFlow = rehabSessionRepository.getRehabSessionsBetween(userId, startDate, endDate)
        val dietFlow = dietSessionRepository.getDietSessionsBetween(userId, startDate, endDate)

        // 3. 두 데이터를 하나의 쌍(Pair)으로 묶어서 반환
        return combine(rehabFlow, dietFlow) { rehabs, diets ->
            Pair(rehabs, diets)
        }
    }
}