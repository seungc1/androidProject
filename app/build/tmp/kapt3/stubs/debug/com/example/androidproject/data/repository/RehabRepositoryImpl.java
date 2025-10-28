package com.example.androidproject.data.repository;

@kotlin.Metadata(mv = {1, 8, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0007\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004H\u0002J\u001f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00050\u00042\u0006\u0010\u0007\u001a\u00020\bH\u0096@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\tJ\u001f\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00050\u00042\u0006\u0010\u000b\u001a\u00020\fH\u0096@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\r\u0082\u0002\u0004\n\u0002\b\u0019\u00a8\u0006\u000e"}, d2 = {"Lcom/example/androidproject/data/repository/RehabRepositoryImpl;", "Lcom/example/androidproject/domain/repository/RehabRepository;", "()V", "generateDummyExercises", "", "Lcom/example/androidproject/domain/model/Exercise;", "getAllExercisesByBodyPart", "bodyPart", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getRecommendedExercises", "injury", "Lcom/example/androidproject/domain/model/Injury;", "(Lcom/example/androidproject/domain/model/Injury;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class RehabRepositoryImpl implements com.example.androidproject.domain.repository.RehabRepository {
    
    @javax.inject.Inject
    public RehabRepositoryImpl() {
        super();
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.Nullable
    public java.lang.Object getRecommendedExercises(@org.jetbrains.annotations.NotNull
    com.example.androidproject.domain.model.Injury injury, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.util.List<com.example.androidproject.domain.model.Exercise>> $completion) {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.Nullable
    public java.lang.Object getAllExercisesByBodyPart(@org.jetbrains.annotations.NotNull
    java.lang.String bodyPart, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.util.List<com.example.androidproject.domain.model.Exercise>> $completion) {
        return null;
    }
    
    private final java.util.List<com.example.androidproject.domain.model.Exercise> generateDummyExercises() {
        return null;
    }
}