package com.example.androidproject.di;

@dagger.Module
@kotlin.Metadata(mv = {1, 8, 0}, k = 1, xi = 48, d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\'\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\'J\u0015\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nH\'\u00a2\u0006\u0002\u0010\u000bJ\u0010\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000fH\'J\u0015\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\nH\'\u00a2\u0006\u0002\u0010\u0013J\u0010\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u0017H\'\u00a8\u0006\u0018"}, d2 = {"Lcom/example/androidproject/di/RepositoryModule;", "", "()V", "bindAIApiRepository", "Lcom/example/androidproject/domain/repository/AIApiRepository;", "aiApiRepositoryImpl", "Lcom/example/androidproject/data/repository/AIApiRepositoryImpl;", "bindDietSessionRepository", "Lcom/example/androidproject/domain/repository/DietSessionRepository;", "dietSessionRepositoryImpl", "error/NonExistentClass", "(Lerror/NonExistentClass;)Lcom/example/androidproject/domain/repository/DietSessionRepository;", "bindRehabRepository", "Lcom/example/androidproject/domain/repository/RehabRepository;", "rehabRepositoryImpl", "Lcom/example/androidproject/data/repository/RehabRepositoryImpl;", "bindRehabSessionRepository", "Lcom/example/androidproject/domain/repository/RehabSessionRepository;", "rehabSessionRepositoryImpl", "(Lerror/NonExistentClass;)Lcom/example/androidproject/domain/repository/RehabSessionRepository;", "bindUserRepository", "Lcom/example/androidproject/domain/repository/UserRepository;", "userRepositoryImpl", "Lcom/example/androidproject/data/repository/UserRepositoryImpl;", "app_debug"})
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
    error.NonExistentClass rehabSessionRepositoryImpl);
    
    @dagger.Binds
    @javax.inject.Singleton
    @org.jetbrains.annotations.NotNull
    public abstract com.example.androidproject.domain.repository.DietSessionRepository bindDietSessionRepository(@org.jetbrains.annotations.NotNull
    error.NonExistentClass dietSessionRepositoryImpl);
}