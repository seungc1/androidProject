package com.dataDoctor.rehabai.data.remote.api

import com.dataDoctor.rehabai.data.remote.dto.AIRecommendationResultDto // (다음 단계에서 만들 예정)
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit을 사용하여 AI 서버와 통신하는 API 명세 인터페이스입니다.
 * (상세 로드맵 Phase 1)
 */
interface ApiService {

    /**
     * AI 추천 운동 및 식단 목록을 가져옵니다.
     * (RehabRepository의 getAIRecommendations 함수를 구현하기 위해 사용됨)
     *
     * @GET("recommendations") // <-- AI 서버의 실제 엔드포인트(URL 경로)
     *
     * @param userId AI 서버에 전달할 사용자 ID
     * @return AIRecommendationResultDto (API의 JSON 응답과 1:1로 매칭되는 DTO)
     */
    @GET("recommendations")
    suspend fun getAIRecommendations(
        @Query("userId") userId: String
    ): Response<AIRecommendationResultDto>

    // (참고)
    // 만약 RehabSession을 서버로 전송(POST)해야 한다면,
    // 나중에 이런 함수가 추가될 것입니다.
    // @POST("log/session")
    // suspend fun logRehabSession(
    //     @Body sessionDto: RehabSessionDto
    // ): Response<Unit> // 성공 여부만 받음
}