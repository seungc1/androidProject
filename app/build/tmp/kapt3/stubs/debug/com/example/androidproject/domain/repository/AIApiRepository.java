package com.example.androidproject.domain.repository;

@kotlin.Metadata(mv = {1, 8, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\bf\u0018\u00002\u00020\u0001J)\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\u0006\u0010\u0005\u001a\u00020\u00062\b\u0010\u0007\u001a\u0004\u0018\u00010\bH\u00a6@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\t\u0082\u0002\u0004\n\u0002\b\u0019\u00a8\u0006\n"}, d2 = {"Lcom/example/androidproject/domain/repository/AIApiRepository;", "", "getAIRehabAndDietRecommendation", "Lkotlinx/coroutines/flow/Flow;", "Lcom/example/androidproject/domain/model/AIRecommendationResult;", "userInfo", "Lcom/example/androidproject/domain/model/User;", "injuryInfo", "Lcom/example/androidproject/domain/model/Injury;", "(Lcom/example/androidproject/domain/model/User;Lcom/example/androidproject/domain/model/Injury;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public abstract interface AIApiRepository {
    
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object getAIRehabAndDietRecommendation(@org.jetbrains.annotations.NotNull
    com.example.androidproject.domain.model.User userInfo, @org.jetbrains.annotations.Nullable
    com.example.androidproject.domain.model.Injury injuryInfo, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlinx.coroutines.flow.Flow<com.example.androidproject.domain.model.AIRecommendationResult>> $completion);
}