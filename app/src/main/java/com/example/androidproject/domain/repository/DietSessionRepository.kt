package com.example.androidproject.domain.repository

import com.example.androidproject.domain.model.DietSession
import kotlinx.coroutines.flow.Flow

interface DietSessionRepository {
    // 새로운 식단 섭취 기록 추가
    suspend fun addDietSession(session: DietSession): Flow<Unit>

    // 특정 사용자의 식단 섭취 기록 목록 가져오기
    suspend fun getDietHistory(userId: String): Flow<List<DietSession>>
}