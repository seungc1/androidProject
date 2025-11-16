package com.example.androidproject.di

import com.example.androidproject.data.repository.AIApiRepositoryImpl
import com.example.androidproject.domain.repository.AIApiRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ReleaseAIApiModule {

    @Binds
    @Singleton
    abstract fun bindAIApiRepository(
        aiApiRepositoryImpl: AIApiRepositoryImpl // 실제 구현체
    ): AIApiRepository
}