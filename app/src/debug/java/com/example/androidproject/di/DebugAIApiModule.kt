package com.example.androidproject.di

import com.example.androidproject.data.repository.FakeAIApiRepository
import com.example.androidproject.domain.repository.AIApiRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.UninstallModules // 👈 (중요) Hilt 모듈 제거 기능 import
import javax.inject.Singleton

// 1. (★핵심★) 'release' 빌드용 모듈을 Hilt에서 제거하도록 설정
@UninstallModules(ReleaseAIApiModule::class)
@Module
@InstallIn(SingletonComponent::class)
abstract class DebugAIApiModule { // 👈 'Debug'용 새 클래스 이름

    // 2. 가짜(Fake) Repository를 주입하도록 설정
    @Binds
    @Singleton
    abstract fun bindAIApiRepository(
        fakeAIApiRepository: FakeAIApiRepository // 👈 FakeAIApiRepository 주입
    ): AIApiRepository
}