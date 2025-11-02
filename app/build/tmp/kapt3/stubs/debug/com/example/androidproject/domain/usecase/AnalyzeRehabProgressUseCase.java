package com.example.androidproject.domain.usecase;

@kotlin.Metadata(mv = {1, 8, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u001f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\b2\u0006\u0010\n\u001a\u00020\u000bH\u0086B\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\fR\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u0082\u0002\u0004\n\u0002\b\u0019\u00a8\u0006\r"}, d2 = {"Lcom/example/androidproject/domain/usecase/AnalyzeRehabProgressUseCase;", "", "rehabSessionRepository", "Lcom/example/androidproject/domain/repository/RehabSessionRepository;", "dietSessionRepository", "Lcom/example/androidproject/domain/repository/DietSessionRepository;", "(Lcom/example/androidproject/domain/repository/RehabSessionRepository;Lcom/example/androidproject/domain/repository/DietSessionRepository;)V", "invoke", "Lkotlinx/coroutines/flow/Flow;", "Lcom/example/androidproject/domain/model/ProgressAnalysisResult;", "userId", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class AnalyzeRehabProgressUseCase {
    @org.jetbrains.annotations.NotNull
    private final com.example.androidproject.domain.repository.RehabSessionRepository rehabSessionRepository = null;
    @org.jetbrains.annotations.NotNull
    private final com.example.androidproject.domain.repository.DietSessionRepository dietSessionRepository = null;
    
    @javax.inject.Inject
    public AnalyzeRehabProgressUseCase(@org.jetbrains.annotations.NotNull
    com.example.androidproject.domain.repository.RehabSessionRepository rehabSessionRepository, @org.jetbrains.annotations.NotNull
    com.example.androidproject.domain.repository.DietSessionRepository dietSessionRepository) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object invoke(@org.jetbrains.annotations.NotNull
    java.lang.String userId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlinx.coroutines.flow.Flow<com.example.androidproject.domain.model.ProgressAnalysisResult>> $completion) {
        return null;
    }
}