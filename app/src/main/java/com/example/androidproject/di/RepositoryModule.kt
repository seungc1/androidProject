// app/src/main/java/com/example/androidproject/di/RepositoryModule.kt
package com.example.androidproject.di

import com.example.androidproject.data.repository.AIApiRepositoryImpl
import com.example.androidproject.data.repository.RehabRepositoryImpl
import com.example.androidproject.data.repository.UserRepositoryImpl
import com.example.androidproject.domain.repository.AIApiRepository
import com.example.androidproject.domain.repository.DietSessionRepository
import com.example.androidproject.domain.repository.RehabRepository
import com.example.androidproject.domain.repository.RehabSessionRepository
import com.example.androidproject.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module // 이 클래스가 Hilt 모듈임을 나타냅니다.
@InstallIn(SingletonComponent::class) // 이 모듈의 바인딩이 앱의 생명주기 전체에 걸쳐 유효하도록 합니다.
abstract class RepositoryModule {

    // RehabRepository 인터페이스가 요청될 때 RehabRepositoryImpl 구현체를 제공하도록 Hilt에 지시합니다.
    @Binds
    @Singleton // RehabRepository 인스턴스가 앱 전체에서 단일 인스턴스로 유지되도록 합니다. (선택 사항, 필요에 따라 제거 가능)
    abstract fun bindRehabRepository(
        rehabRepositoryImpl: RehabRepositoryImpl
    ): RehabRepository
}

@Module
@InstallIn(SingletonComponent::class) // 앱의 수명 주기와 함께 유지됩니다.
abstract class RepositoryModule {

    // RehabRepository 인터페이스에 RehabRepositoryImpl 구현체를 바인딩
    @Binds
    @Singleton
    abstract fun bindRehabRepository(
        rehabRepositoryImpl: RehabRepositoryImpl
    ): RehabRepository

    // UserRepository 인터페이스에 UserRepositoryImpl 구현체를 바인딩
    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    // AIApiRepository 인터페이스에 AIApiRepositoryImpl 구현체를 바인딩
    @Binds
    @Singleton
    abstract fun bindAIApiRepository(
        aiApiRepositoryImpl: AIApiRepositoryImpl
    ): AIApiRepository

    // RehabSessionRepository 인터페이스에 RehabSessionRepositoryImpl 구현체를 바인딩 ✨ 추가 ✨
    @Binds
    @Singleton
    abstract fun bindRehabSessionRepository(
        rehabSessionRepositoryImpl: RehabSessionRepositoryImpl
    ): RehabSessionRepository

    // DietSessionRepository 인터페이스에 DietSessionRepositoryImpl 구현체를 바인딩 ✨ 추가 ✨
    @Binds
    @Singleton
    abstract fun bindDietSessionRepository(
        dietSessionRepositoryImpl: DietSessionRepositoryImpl
    ): DietSessionRepository
}