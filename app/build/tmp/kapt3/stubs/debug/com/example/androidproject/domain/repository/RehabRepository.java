package com.example.androidproject.domain.repository;

@kotlin.Metadata(mv = {1, 8, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\bf\u0018\u00002\u00020\u0001J\u001f\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\u0006\u0010\u0005\u001a\u00020\u0006H\u00a6@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0007J\u001f\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\u0006\u0010\t\u001a\u00020\nH\u00a6@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u000b\u0082\u0002\u0004\n\u0002\b\u0019\u00a8\u0006\f"}, d2 = {"Lcom/example/androidproject/domain/repository/RehabRepository;", "", "getAllExercisesByBodyPart", "", "Lcom/example/androidproject/domain/model/Exercise;", "bodyPart", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getRecommendedExercises", "injury", "Lcom/example/androidproject/domain/model/Injury;", "(Lcom/example/androidproject/domain/model/Injury;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public abstract interface RehabRepository {
    
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object getRecommendedExercises(@org.jetbrains.annotations.NotNull
    com.example.androidproject.domain.model.Injury injury, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.util.List<com.example.androidproject.domain.model.Exercise>> $completion);
    
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object getAllExercisesByBodyPart(@org.jetbrains.annotations.NotNull
    java.lang.String bodyPart, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.util.List<com.example.androidproject.domain.model.Exercise>> $completion);
}