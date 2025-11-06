package com.example.androidproject.domain.usecase;

@kotlin.Metadata(mv = {1, 8, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J)\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\b2\u0006\u0010\n\u001a\u00020\u000b2\b\u0010\f\u001a\u0004\u0018\u00010\rH\u0086B\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u000eR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u0082\u0002\u0004\n\u0002\b\u0019\u00a8\u0006\u000f"}, d2 = {"Lcom/example/androidproject/domain/usecase/GetAIRecommendationUseCase;", "", "aiApiRepository", "Lcom/example/androidproject/domain/repository/AIApiRepository;", "userRepository", "Lcom/example/androidproject/domain/repository/UserRepository;", "(Lcom/example/androidproject/domain/repository/AIApiRepository;Lcom/example/androidproject/domain/repository/UserRepository;)V", "invoke", "Lkotlinx/coroutines/flow/Flow;", "Lcom/example/androidproject/domain/model/AIRecommendationResult;", "userId", "", "injuryInfo", "Lcom/example/androidproject/domain/model/Injury;", "(Ljava/lang/String;Lcom/example/androidproject/domain/model/Injury;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class GetAIRecommendationUseCase {
    @org.jetbrains.annotations.NotNull
    private final com.example.androidproject.domain.repository.AIApiRepository aiApiRepository = null;
    @org.jetbrains.annotations.NotNull
    private final com.example.androidproject.domain.repository.UserRepository userRepository = null;
    
    @javax.inject.Inject
    public GetAIRecommendationUseCase(@org.jetbrains.annotations.NotNull
    com.example.androidproject.domain.repository.AIApiRepository aiApiRepository, @org.jetbrains.annotations.NotNull
    com.example.androidproject.domain.repository.UserRepository userRepository) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object invoke(@org.jetbrains.annotations.NotNull
    java.lang.String userId, @org.jetbrains.annotations.Nullable
    com.example.androidproject.domain.model.Injury injuryInfo, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlinx.coroutines.flow.Flow<com.example.androidproject.domain.model.AIRecommendationResult>> $completion) {
        return null;
    }
}