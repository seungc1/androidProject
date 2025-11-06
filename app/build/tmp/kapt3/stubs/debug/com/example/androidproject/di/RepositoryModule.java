package com.example.androidproject.di;

@dagger.Module
@kotlin.Metadata(mv = {1, 8, 0}, k = 1, xi = 48, d1 = {"\u0000H\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\'\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\'J\u0010\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nH\'J\u0010\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000eH\'J\u0010\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u0012H\'J\u0010\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u0016H\'\u00a8\u0006\u0017"}, d2 = {"Lcom/example/androidproject/di/RepositoryModule;", "", "()V", "bindAIApiRepository", "Lcom/example/androidproject/domain/repository/AIApiRepository;", "aiApiRepositoryImpl", "Lcom/example/androidproject/data/repository/AIApiRepositoryImpl;", "bindDietSessionRepository", "Lcom/example/androidproject/domain/repository/DietSessionRepository;", "dietSessionRepositoryImpl", "Lcom/example/androidproject/data/repository/DietSessionRepositoryImpl;", "bindRehabRepository", "Lcom/example/androidproject/domain/repository/RehabRepository;", "rehabRepositoryImpl", "Lcom/example/androidproject/data/repository/RehabRepositoryImpl;", "bindRehabSessionRepository", "Lcom/example/androidproject/domain/repository/RehabSessionRepository;", "rehabSessionRepositoryImpl", "Lcom/example/androidproject/data/repository/RehabSessionRepositoryImpl;", "bindUserRepository", "Lcom/example/androidproject/domain/repository/UserRepository;", "userRepositoryImpl", "Lcom/example/androidproject/data/repository/UserRepositoryImpl;", "app_debug"})
@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
public abstract class RepositoryModule {
    
    public RepositoryModule() {
        super();
    }
    
    @dagger.Binds
    @javax.inject.Singleton
    @org.jetbrains.annotations.NotNull
    public abstract com.example.androidproject.domain.repository.RehabRepository bindRehabRepository(@org.jetbrains.annotations.NotNull
    com.example.androidproject.data.repository.RehabRepositoryImpl rehabRepositoryImpl);
    
    @dagger.Binds
    @javax.inject.Singleton
    @org.jetbrains.annotations.NotNull
    public abstract com.example.androidproject.domain.repository.UserRepository bindUserRepository(@org.jetbrains.annotations.NotNull
    com.example.androidproject.data.repository.UserRepositoryImpl userRepositoryImpl);
    
    @dagger.Binds
    @javax.inject.Singleton
    @org.jetbrains.annotations.NotNull
    public abstract com.example.androidproject.domain.repository.AIApiRepository bindAIApiRepository(@org.jetbrains.annotations.NotNull
    com.example.androidproject.data.repository.AIApiRepositoryImpl aiApiRepositoryImpl);
    
    @dagger.Binds
    @javax.inject.Singleton
    @org.jetbrains.annotations.NotNull
    public abstract com.example.androidproject.domain.repository.RehabSessionRepository bindRehabSessionRepository(@org.jetbrains.annotations.NotNull
    com.example.androidproject.data.repository.RehabSessionRepositoryImpl rehabSessionRepositoryImpl);
    
    @dagger.Binds
    @javax.inject.Singleton
    @org.jetbrains.annotations.NotNull
    public abstract com.example.androidproject.domain.repository.DietSessionRepository bindDietSessionRepository(@org.jetbrains.annotations.NotNull
    com.example.androidproject.data.repository.DietSessionRepositoryImpl dietSessionRepositoryImpl);
}