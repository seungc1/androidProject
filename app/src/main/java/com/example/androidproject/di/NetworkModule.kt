package com.example.androidproject.di

import com.example.androidproject.BuildConfig
import com.example.androidproject.data.network.GptApiService
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
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
        // 1. API 키 가져오기
        val apiKey = BuildConfig.GPT_API_KEY
        android.util.Log.d("API_KEY_CHECK", "Current Key: $apiKey")

        // 2. 인증 헤더를 추가하는 Interceptor 생성 (핵심)
        val authInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val newRequest = originalRequest.newBuilder()
                // "Bearer " 뒤에 키가 붙을 때 공백이 정확히 하나인지 확인하세요.
                .header("Authorization", "Bearer $apiKey")
                .build()
            chain.proceed(newRequest)
        }

        // 3. 로그 인터셉터 (디버깅용)
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor) // 인증 인터셉터 등록
            .addInterceptor(logging)         // 로그 인터셉터 등록
            // GPT API는 응답 시간이 길 수 있으므로 타임아웃을 60초로 설정
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

    @Provides
    @Singleton
    fun provideGptApiService(retrofit: Retrofit): GptApiService {
        return retrofit.create(GptApiService::class.java)
    }
}