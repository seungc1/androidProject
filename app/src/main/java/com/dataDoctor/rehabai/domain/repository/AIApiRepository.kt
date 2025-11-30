// app/src/main/java/com/example/androidproject/domain/repository/AIApiRepository.kt
package com.dataDoctor.rehabai.domain.repository

import com.dataDoctor.rehabai.domain.model.AIAnalysisResult
import com.dataDoctor.rehabai.domain.model.AIRecommendationResult
import com.dataDoctor.rehabai.domain.model.RehabData
import com.dataDoctor.rehabai.domain.model.RecommendationParams
import kotlinx.coroutines.flow.Flow

/**
 * AI API(GPT)와의 통신을 위한 추상화 인터페이스.
 * Data Layer에서 이 인터페이스를 구현하여 GPT API 호출 로직을 담당합니다.
 */
interface AIApiRepository {
    /**
     * 사용자의 정보를 기반으로 GPT API에 재활 운동 및 식단 추천을 요청합니다.
     * 이 함수는 GPT API에 전달할 프롬프트 구성에 필요한 파라미터 DTO를 받습니다.
     * Data Layer에서 이 파라미터 DTO를 GPT 프롬프트로 변환하고, GPT 응답을 파싱하여
     * AIRecommendationResult 객체로 변환하는 로직을 구현해야 합니다.
     *
     * @param params AI 추천에 필요한 사용자 입력 파라미터 (User 및 Injury 정보를 포함).
     * @return GPT API로부터 받은 AI 추천 결과 (파싱된 AIRecommendationResult).
     */
    suspend fun getAIRehabAndDietRecommendation(params: RecommendationParams): Flow<AIRecommendationResult>

    suspend fun analyzeRehabProgress(rehabData: RehabData): Flow<AIAnalysisResult>
}