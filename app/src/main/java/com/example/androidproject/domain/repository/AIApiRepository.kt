package com.example.androidproject.domain.repository

import com.example.androidproject.domain.model.AIRecommendationResult
import com.example.androidproject.domain.model.Injury
import com.example.androidproject.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AIApiRepository {
    // 사용자 정보와 부상 정보를 기반으로 AI로부터 운동 및 식단 추천을 요청
    suspend fun getAIRehabAndDietRecommendation(
        userInfo: User,
        injuryInfo: Injury? // 부상 정보는 없을 수도 있으므로 Nullable 처리
    ): Flow<AIRecommendationResult>
}