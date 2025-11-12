package com.example.androidproject.di

import com.example.androidproject.domain.repository.AIApiRepository
import com.example.androidproject.domain.repository.DietSessionRepository
import com.example.androidproject.domain.repository.RehabRepository
import com.example.androidproject.domain.repository.RehabSessionRepository
import com.example.androidproject.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// @TestInstallIn 어노테이션은 이 모듈이 어떤 Hilt 컴포넌트에 설치될지를 지정하며,
// 특히 테스트 시에 다른 모듈을 대체하도록 합니다.
// 'replaces' 속성으로 실제 앱의 RepositoryModule을 대체하도록 설정합니다.
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RepositoryModule::class] // 실제 RepositoryModule을 이 테스트 모듈로 대체합니다.
)
abstract class TestRepositoryModule {

    // AIApiRepository 인터페이스가 요청될 때 FakeAIApiRepository 구현체를 제공하도록 바인딩
    @Binds
    @Singleton
    abstract fun bindAIApiRepository(
        fakeAIApiRepository: FakeAIApiRepository
    ): AIApiRepository

    // UserRepository 인터페이스가 요청될 때 FakeUserRepository 구현체를 제공하도록 바인딩
    @Binds
    @Singleton
    abstract fun bindUserRepository(
        fakeUserRepository: FakeUserRepository
    ): UserRepository

    // RehabRepository 인터페이스가 요청될 때 FakeRehabRepository 구현체를 제공하도록 바인딩
    @Binds
    @Singleton
    abstract fun bindRehabRepository(
        fakeRehabRepository: FakeRehabRepository
    ): RehabRepository

    // RehabSessionRepository 인터페이스가 요청될 때 FakeRehabSessionRepository 구현체를 제공하도록 바인딩
    @Binds
    @Singleton
    abstract fun bindRehabSessionRepository(
        fakeRehabSessionRepository: FakeRehabSessionRepository
    ): RehabSessionRepository

    // DietSessionRepository 인터페이스가 요청될 때 FakeDietSessionRepository 구현체를 제공하도록 바인딩
    @Binds
    @Singleton
    abstract fun bindDietSessionRepository(
        fakeDietSessionRepository: FakeDietSessionRepository
    ): DietSessionRepository
}