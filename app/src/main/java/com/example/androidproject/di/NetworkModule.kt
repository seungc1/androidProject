package com.example.androidproject.di

import com.example.androidproject.data.network.GptApiService
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // (API 제공자에 따라 기본 URL 변경)
    private const val GPT_API_BASE_URL = "https://api.openai.com/"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            // GPT API는 응답 시간이 길 수 있으므로 타임아웃을 60초로 늘립니다.
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return Gson()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(GPT_API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson)) // Gson 컨버터 사용
            .build()
    }

    /**
     * Hilt가 'GptApiService'를 요청할 때
     * 이 함수가 Retrofit을 사용하여 GptApiService의 구현체를 생성합니다.
     */
    @Provides
    @Singleton
    fun provideGptApiService(retrofit: Retrofit): GptApiService {
        return retrofit.create(GptApiService::class.java)
    }
}