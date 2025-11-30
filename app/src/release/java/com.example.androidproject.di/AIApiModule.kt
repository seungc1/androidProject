package com.dataDoctor.rehabai.di

import com.dataDoctor.rehabai.data.repository.AIApiRepositoryImpl
import com.dataDoctor.rehabai.domain.repository.AIApiRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * [Release 모드용]
 * 실제 앱 출시 시에만 포함되는 모듈입니다.
 * 실제 API 구현체(AIApiRepositoryImpl)를 연결합니다.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AIApiModule {

    @Binds
    @Singleton
    abstract fun bindAIApiRepository(
        aiApiRepositoryImpl: AIApiRepositoryImpl
    ): AIApiRepository
}