package com.example.androidproject.data.repository;

@kotlin.Metadata(mv = {1, 8, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0007\u00a2\u0006\u0002\u0010\u0002J)\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u00042\u0006\u0010\u0006\u001a\u00020\u00072\b\u0010\b\u001a\u0004\u0018\u00010\tH\u0096@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\n\u0082\u0002\u0004\n\u0002\b\u0019\u00a8\u0006\u000b"}, d2 = {"Lcom/example/androidproject/data/repository/AIApiRepositoryImpl;", "Lcom/example/androidproject/domain/repository/AIApiRepository;", "()V", "getAIRehabAndDietRecommendation", "Lkotlinx/coroutines/flow/Flow;", "Lcom/example/androidproject/domain/model/AIRecommendationResult;", "userInfo", "Lcom/example/androidproject/domain/model/User;", "injuryInfo", "Lcom/example/androidproject/domain/model/Injury;", "(Lcom/example/androidproject/domain/model/User;Lcom/example/androidproject/domain/model/Injury;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class AIApiRepositoryImpl implements com.example.androidproject.domain.repository.AIApiRepository {
    
    @javax.inject.Inject
    public AIApiRepositoryImpl() {
        super();
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.Nullable
    public java.lang.Object getAIRehabAndDietRecommendation(@org.jetbrains.annotations.NotNull
    com.example.androidproject.domain.model.User userInfo, @org.jetbrains.annotations.Nullable
    com.example.androidproject.domain.model.Injury injuryInfo, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlinx.coroutines.flow.Flow<com.example.androidproject.domain.model.AIRecommendationResult>> $completion) {
        return null;
    }
}