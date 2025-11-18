package com.example.androidproject.data.network

import com.example.androidproject.data.network.dto.GptRequest
import com.example.androidproject.data.network.dto.GptResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface GptApiService {

    @POST("v1/chat/completions")
    suspend fun getChatCompletion(
        // Interceptor에서 처리하므로 @Header 파라미터 삭제됨
        @Body request: GptRequest
    ): GptResponse
}