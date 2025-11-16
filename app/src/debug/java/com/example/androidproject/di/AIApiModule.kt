package com.example.androidproject.di

import com.example.androidproject.data.repository.FakeAIApiRepository
import com.example.androidproject.domain.repository.AIApiRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * [Debug 모드용]
 * 개발(Debug) 빌드 시에만 포함되는 모듈입니다.
 * 가짜 구현체(FakeAIApiRepository)를 연결합니다.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AIApiModule {

    @Binds
    @Singleton
    abstract fun bindAIApiRepository(
        fakeAIApiRepository: FakeAIApiRepository
    ): AIApiRepository
}