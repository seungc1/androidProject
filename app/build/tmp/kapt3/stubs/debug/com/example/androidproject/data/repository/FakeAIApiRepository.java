package com.example.androidproject.data.repository;

@kotlin.Metadata(mv = {1, 8, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\n\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J)\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00040\u00112\u0006\u0010\u0012\u001a\u00020\u00132\b\u0010\u0014\u001a\u0004\u0018\u00010\u0015H\u0096@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0016R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0005\u001a\u00020\u0006X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0007\u0010\b\"\u0004\b\t\u0010\nR\u001c\u0010\u000b\u001a\u0004\u0018\u00010\u0004X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\f\u0010\r\"\u0004\b\u000e\u0010\u000f\u0082\u0002\u0004\n\u0002\b\u0019\u00a8\u0006\u0017"}, d2 = {"Lcom/example/androidproject/data/repository/FakeAIApiRepository;", "Lcom/example/androidproject/domain/repository/AIApiRepository;", "()V", "defaultResult", "Lcom/example/androidproject/domain/model/AIRecommendationResult;", "shouldReturnError", "", "getShouldReturnError", "()Z", "setShouldReturnError", "(Z)V", "testResult", "getTestResult", "()Lcom/example/androidproject/domain/model/AIRecommendationResult;", "setTestResult", "(Lcom/example/androidproject/domain/model/AIRecommendationResult;)V", "getAIRehabAndDietRecommendation", "Lkotlinx/coroutines/flow/Flow;", "userInfo", "Lcom/example/androidproject/domain/model/User;", "injuryInfo", "Lcom/example/androidproject/domain/model/Injury;", "(Lcom/example/androidproject/domain/model/User;Lcom/example/androidproject/domain/model/Injury;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class FakeAIApiRepository implements com.example.androidproject.domain.repository.AIApiRepository {
    private boolean shouldReturnError = false;
    @org.jetbrains.annotations.Nullable
    private com.example.androidproject.domain.model.AIRecommendationResult testResult;
    @org.jetbrains.annotations.NotNull
    private final com.example.androidproject.domain.model.AIRecommendationResult defaultResult = null;
    
    public FakeAIApiRepository() {
        super();
    }
    
    public final boolean getShouldReturnError() {
        return false;
    }
    
    public final void setShouldReturnError(boolean p0) {
    }
    
    @org.jetbrains.annotations.Nullable
    public final com.example.androidproject.domain.model.AIRecommendationResult getTestResult() {
        return null;
    }
    
    public final void setTestResult(@org.jetbrains.annotations.Nullable
    com.example.androidproject.domain.model.AIRecommendationResult p0) {
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