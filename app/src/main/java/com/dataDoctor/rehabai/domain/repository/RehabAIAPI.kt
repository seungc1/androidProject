// app/src/main/java/com/example/androidproject/domain/repository/RehabAIAPI.kt

package com.dataDoctor.rehabai.domain.repository

import com.dataDoctor.rehabai.domain.model.AIAnalysisResult
import com.dataDoctor.rehabai.domain.model.AIRecommendation
import com.dataDoctor.rehabai.domain.model.RehabData
import com.dataDoctor.rehabai.domain.model.RecommendationParams
import kotlinx.coroutines.flow.Flow

/**
 * AI 모델(GPT API)과의 상호작용을 위한 추상화 인터페이스.
 * Data Layer에서 이 인터페이스를 구현하여 GPT API 호출 로직을 담당합니다.
 */
interface RehabAIAPI {

    /**
     * 사용자의 필드값(부상 부위, 상태, 목표 등)을 기반으로 GPT API에 맞춤형 재활 운동 및 식단을 요청합니다.
     *
     * @param params AI 추천에 필요한 사용자 입력 파라미터.
     * @return GPT API로부터 받은 AI 추천 결과 (운동 리스트, 식단 리스트 등).
     */
    suspend fun getRecommendation(params: RecommendationParams): Flow<AIRecommendation>

    /**
     * 사용자의 기록된 재활 세션 데이터를 GPT API에 전달하여 진행 상황을 분석하고 피드백을 받습니다.
     *
     * @param data AI 분석에 필요한 사용자의 재활 기록 데이터.
     * @return GPT API로부터 받은 AI 분석 결과 및 피드백.
     */
    suspend fun analyzeProgress(data: RehabData): Flow<AIAnalysisResult>
}