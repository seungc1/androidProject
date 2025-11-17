package com.example.androidproject.data.remote.datasource

import com.example.androidproject.data.remote.api.ApiService
import com.example.androidproject.data.remote.dto.AIRecommendationResultDto
import retrofit2.Response
import javax.inject.Inject

/**
 * 'ApiService'를 실제로 호출하여 Remote(원격) 데이터를 가져오는 클래스
 *
 * Hilt가 'ApiService'를 여기에 주입(@Inject)해 줍니다.
 */
class RemoteDataSource @Inject constructor(
    private val apiService: ApiService
) {

    /**
     * ApiService를 호출하여 AI 추천 데이터를 DTO 형태로 가져옵니다.
     * * @param userId API에 전달할 사용자 ID
     * @return API 응답 (Response<AIRecommendationResultDto>)
     */
    suspend fun fetchAIRecommendations(userId: String): Response<AIRecommendationResultDto> {
        // Hilt가 주입해준 apiService의 함수를 실제로 호출합니다.
        return apiService.getAIRecommendations(userId)
    }

    // (나중에 RehabSession을 서버로 전송하는 기능이 필요하면
    //  여기에 suspend fun logRehabSession(sessionDto: ...) 함수를 추가합니다.)
}