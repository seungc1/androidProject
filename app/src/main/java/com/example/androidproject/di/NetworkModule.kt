package com.example.androidproject.di

import com.example.androidproject.BuildConfig
import com.example.androidproject.data.network.GptApiService
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val GPT_API_BASE_URL = "https://api.openai.com/"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {

        val logger = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY // 개발(debug) 중에는 모든 로그를 본다
            } else {
                HttpLoggingInterceptor.Level.NONE // 출시(release) 후에는 로그를 남기지 않는다
            }
        }

        return OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(logger)
            .build()
    }

    // (provideGson, provideRetrofit, provideGptApiService 함수는 수정 없음)
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
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideGptApiService(retrofit: Retrofit): GptApiService {
        return retrofit.create(GptApiService::class.java)
    }
}