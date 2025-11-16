package com.example.androidproject.di

// (★ 추가 ★) 'Hilt' '테스트' '어노테이션' 'import'
import dagger.hilt.testing.TestInstallIn

// (★ 추가 ★) '가짜' 'Repository' '부품' 'import'
import com.example.androidproject.data.repository.FakeAIApiRepository
import com.example.androidproject.data.repository.FakeDietSessionRepository
import com.example.androidproject.data.repository.FakeRehabRepository
import com.example.androidproject.data.repository.FakeRehabSessionRepository
import com.example.androidproject.data.repository.FakeUserRepository

// (★ 기존 ★) '진짜' 'Repository' '인터페이스' 'import'
import com.example.androidproject.domain.repository.AIApiRepository
import com.example.androidproject.domain.repository.DietSessionRepository
import com.example.androidproject.domain.repository.RehabRepository
import com.example.androidproject.domain.repository.RehabSessionRepository
import com.example.androidproject.domain.repository.UserRepository

import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * (★ 수정 ★)
 * '테스트' '모듈'이 'RepositoryModule' '뿐만' '아니라'
 * 'AIApiModule'도 '대체'하도록 'replaces' '속성'을 '수정'합니다.
 */
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    // (★ 수정 ★) 'AIApiModule' '교체' '추가'
    replaces = [RepositoryModule::class, AIApiModule::class]
)
abstract class TestRepositoryModule {

    // AIApiRepository 인터페이스가 요청될 때 FakeAIApiRepository 구현체를 제공하도록 바인딩
    @Binds
    @Singleton
    abstract fun bindAIApiRepository(
        fakeAIApiRepository: FakeAIApiRepository // (이제 'Unresolved' '오류'가 '사라져야' '합니다')
    ): AIApiRepository

    // UserRepository 인터페이스가 요청될 때 FakeUserRepository 구현체를 제공하도록 바인딩
    @Binds
    @Singleton
    abstract fun bindUserRepository(
        fakeUserRepository: FakeUserRepository // (이제 'Unresolved' '오류'가 '사라져야' '합니다')
    ): UserRepository

    // RehabRepository 인터페이스가 요청될 때 FakeRehabRepository 구현체를 제공하도록 바인딩
    @Binds
    @Singleton
    abstract fun bindRehabRepository(
        fakeRehabRepository: FakeRehabRepository // (이제 'Unresolved' '오류'가 '사라져야' '합니다')
    ): RehabRepository

    // RehabSessionRepository 인터페이스가 요청될 때 FakeRehabSessionRepository 구현체를 제공하도록 바인딩
    @Binds
    @Singleton
    abstract fun bindRehabSessionRepository(
        fakeRehabSessionRepository: FakeRehabSessionRepository // (이제 'Unresolved' '오류'가 '사라져야' '합니다')
    ): RehabSessionRepository

    // DietSessionRepository 인터페이스가 요청될 때 FakeDietSessionRepository 구현체를 제공하도록 바인딩
    @Binds
    @Singleton
    abstract fun bindDietSessionRepository(
        fakeDietSessionRepository: FakeDietSessionRepository // (이제 'Unresolved' '오류'가 '사라져야' '합니다')
    ): DietSessionRepository
}