package com.example.androidproject.domain.repository

import com.example.androidproject.domain.model.RehabSession
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface RehabSessionRepository {
    // 새로운 재활 운동 기록 추가
    suspend fun addRehabSession(session: RehabSession): Flow<Unit>

    // 특정 사용자의 재활 운동 기록 목록 가져오기
    suspend fun getRehabHistory(userId: String): Flow<List<RehabSession>>

    // 특정 날짜 범위의 기록을 가져오는 함수
    suspend fun getRehabSessionsBetween(userId: String, startDate: Date, endDate: Date): Flow<List<RehabSession>>
}