// data/network/GptApiService.kt
package com.example.androidproject.data.network

import com.example.androidproject.BuildConfig
import com.example.androidproject.data.network.dto.GptRequest
import com.example.androidproject.data.network.dto.GptResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface GptApiService {

    @POST("v1/chat/completions") // (예시: OpenAI API 엔드포인트)
    suspend fun getChatCompletion(
        @Header("Authorization") apiKey: String = "Bearer ${BuildConfig.GPT_API_KEY}",
        @Body request: GptRequest
    ): GptResponse
}