package com.example.androidproject.di

// Data Layer의 Repository 구현체들을 import
import com.example.androidproject.data.repository.DietSessionRepositoryImpl
import com.example.androidproject.data.repository.RehabRepositoryImpl
import com.example.androidproject.data.repository.RehabSessionRepositoryImpl
import com.example.androidproject.data.repository.UserRepositoryImpl

// Domain Layer의 Repository 인터페이스들을 import
import com.example.androidproject.domain.repository.DietSessionRepository
import com.example.androidproject.domain.repository.RehabRepository
import com.example.androidproject.domain.repository.RehabSessionRepository
import com.example.androidproject.domain.repository.UserRepository

// Dagger/Hilt 관련 import
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module // 이 클래스가 Hilt 모듈임을 나타냅니다.
@InstallIn(SingletonComponent::class) // 이 모듈의 바인딩이 앱의 생명주기 전체에 걸쳐 유효하도록 합니다.
abstract class RepositoryModule { //  RepositoryModule 클래스 선언은 여기에 단 한 번만 있어야 합니다.

    // RehabRepository 인터페이스가 요청될 때 RehabRepositoryImpl 구현체를 제공하도록 Hilt에 지시합니다.
    @Binds
    @Singleton // RehabRepository 인스턴스가 앱 전체에서 단일 인스턴스로 유지되도록 합니다.
    abstract fun bindRehabRepository(
        rehabRepositoryImpl: RehabRepositoryImpl
    ): RehabRepository

    // UserRepository 인터페이스에 UserRepositoryImpl 구현체를 바인딩
    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    // RehabSessionRepository 인터페이스에 RehabSessionRepositoryImpl 구현체를 바인딩
    @Binds
    @Singleton
    abstract fun bindRehabSessionRepository(
        rehabSessionRepositoryImpl: RehabSessionRepositoryImpl
    ): RehabSessionRepository

    // DietSessionRepository 인터페이스에 DietSessionRepositoryImpl 구현체를 바인딩
    @Binds
    @Singleton
    abstract fun bindDietSessionRepository(
        dietSessionRepositoryImpl: DietSessionRepositoryImpl
    ): DietSessionRepository
}