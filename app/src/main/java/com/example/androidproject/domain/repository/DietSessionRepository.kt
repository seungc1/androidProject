package com.example.androidproject.domain.repository

import com.example.androidproject.domain.model.DietSession
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface DietSessionRepository {
    // 새로운 식단 섭취 기록 추가
    suspend fun addDietSession(session: DietSession): Flow<Unit>

    // 특정 사용자의 식단 섭취 기록 목록 가져오기
    suspend fun getDietHistory(userId: String): Flow<List<DietSession>>
    //  특정 날짜 범위의 기록을 가져오는 함
    suspend fun getDietSessionsBetween(userId: String, startDate: Date, endDate: Date): Flow<List<DietSession>>
}