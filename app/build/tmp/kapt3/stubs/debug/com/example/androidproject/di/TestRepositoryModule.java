package com.example.androidproject.di;

@dagger.Module
@kotlin.Metadata(mv = {1, 8, 0}, k = 1, xi = 48, d1 = {"\u0000H\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\'\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\'J\u0010\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nH\'J\u0010\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000eH\'J\u0010\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u0012H\'J\u0010\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u0016H\'\u00a8\u0006\u0017"}, d2 = {"Lcom/example/androidproject/di/TestRepositoryModule;", "", "()V", "bindAIApiRepository", "Lcom/example/androidproject/domain/repository/AIApiRepository;", "fakeAIApiRepository", "Lcom/example/androidproject/data/repository/FakeAIApiRepository;", "bindDietSessionRepository", "Lcom/example/androidproject/domain/repository/DietSessionRepository;", "fakeDietSessionRepository", "Lcom/example/androidproject/data/repository/FakeDietSessionRepository;", "bindRehabRepository", "Lcom/example/androidproject/domain/repository/RehabRepository;", "fakeRehabRepository", "Lcom/example/androidproject/data/repository/FakeRehabRepository;", "bindRehabSessionRepository", "Lcom/example/androidproject/domain/repository/RehabSessionRepository;", "fakeRehabSessionRepository", "Lcom/example/androidproject/data/repository/FakeRehabSessionRepository;", "bindUserRepository", "Lcom/example/androidproject/domain/repository/UserRepository;", "fakeUserRepository", "Lcom/example/androidproject/data/repository/FakeUserRepository;", "app_debug"})
@dagger.hilt.testing.TestInstallIn(components = {dagger.hilt.components.SingletonComponent.class}, replaces = {com.example.androidproject.di.RepositoryModule.class})
public abstract class TestRepositoryModule {
    
    public TestRepositoryModule() {
        super();
    }
    
    @dagger.Binds
    @javax.inject.Singleton
    @org.jetbrains.annotations.NotNull
    public abstract com.example.androidproject.domain.repository.AIApiRepository bindAIApiRepository(@org.jetbrains.annotations.NotNull
    com.example.androidproject.data.repository.FakeAIApiRepository fakeAIApiRepository);
    
    @dagger.Binds
    @javax.inject.Singleton
    @org.jetbrains.annotations.NotNull
    public abstract com.example.androidproject.domain.repository.UserRepository bindUserRepository(@org.jetbrains.annotations.NotNull
    com.example.androidproject.data.repository.FakeUserRepository fakeUserRepository);
    
    @dagger.Binds
    @javax.inject.Singleton
    @org.jetbrains.annotations.NotNull
    public abstract com.example.androidproject.domain.repository.RehabRepository bindRehabRepository(@org.jetbrains.annotations.NotNull
    com.example.androidproject.data.repository.FakeRehabRepository fakeRehabRepository);
    
    @dagger.Binds
    @javax.inject.Singleton
    @org.jetbrains.annotations.NotNull
    public abstract com.example.androidproject.domain.repository.RehabSessionRepository bindRehabSessionRepository(@org.jetbrains.annotations.NotNull
    com.example.androidproject.data.repository.FakeRehabSessionRepository fakeRehabSessionRepository);
    
    @dagger.Binds
    @javax.inject.Singleton
    @org.jetbrains.annotations.NotNull
    public abstract com.example.androidproject.domain.repository.DietSessionRepository bindDietSessionRepository(@org.jetbrains.annotations.NotNull
    com.example.androidproject.data.repository.FakeDietSessionRepository fakeDietSessionRepository);
}