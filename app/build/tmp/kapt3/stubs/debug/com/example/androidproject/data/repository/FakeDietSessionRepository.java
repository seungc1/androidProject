package com.example.androidproject.data.repository;

@kotlin.Metadata(mv = {1, 8, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\b\u0005\n\u0002\u0010\u000e\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u001f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\b2\u0006\u0010\n\u001a\u00020\u0006H\u0096@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u000bJ\u000e\u0010\f\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u0006J%\u0010\r\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u00050\b2\u0006\u0010\u000e\u001a\u00020\u000fH\u0096@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0010R\u001a\u0010\u0003\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u0082\u0002\u0004\n\u0002\b\u0019\u00a8\u0006\u0011"}, d2 = {"Lcom/example/androidproject/data/repository/FakeDietSessionRepository;", "Lcom/example/androidproject/domain/repository/DietSessionRepository;", "()V", "_dietSessions", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "Lcom/example/androidproject/domain/model/DietSession;", "addDietSession", "Lkotlinx/coroutines/flow/Flow;", "", "session", "(Lcom/example/androidproject/domain/model/DietSession;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "addTestSession", "getDietHistory", "userId", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class FakeDietSessionRepository implements com.example.androidproject.domain.repository.DietSessionRepository {
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.example.androidproject.domain.model.DietSession>> _dietSessions = null;
    
    public FakeDietSessionRepository() {
        super();
    }
    
    public final void addTestSession(@org.jetbrains.annotations.NotNull
    com.example.androidproject.domain.model.DietSession session) {
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.Nullable
    public java.lang.Object addDietSession(@org.jetbrains.annotations.NotNull
    com.example.androidproject.domain.model.DietSession session, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlinx.coroutines.flow.Flow<kotlin.Unit>> $completion) {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.Nullable
    public java.lang.Object getDietHistory(@org.jetbrains.annotations.NotNull
    java.lang.String userId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlinx.coroutines.flow.Flow<? extends java.util.List<com.example.androidproject.domain.model.DietSession>>> $completion) {
        return null;
    }
}