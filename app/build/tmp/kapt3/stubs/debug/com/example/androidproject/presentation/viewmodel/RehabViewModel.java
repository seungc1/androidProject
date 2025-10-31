package com.example.androidproject.presentation.viewmodel;

@kotlin.Metadata(mv = {1, 8, 0}, k = 1, xi = 48, d1 = {"\u0000N\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\n\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0018\u0010\u001c\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\t2\b\u0010\u001f\u001a\u0004\u0018\u00010 R\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\b\u001a\b\u0012\u0004\u0012\u00020\t0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000e0\r0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u000f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00100\r0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00070\u00128F\u00a2\u0006\u0006\u001a\u0004\b\u0013\u0010\u0014R\u0017\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\t0\u00128F\u00a2\u0006\u0006\u001a\u0004\b\u0016\u0010\u0014R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u000b0\u00128F\u00a2\u0006\u0006\u001a\u0004\b\u0017\u0010\u0014R\u001d\u0010\u0018\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000e0\r0\u00128F\u00a2\u0006\u0006\u001a\u0004\b\u0019\u0010\u0014R\u001d\u0010\u001a\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00100\r0\u00128F\u00a2\u0006\u0006\u001a\u0004\b\u001b\u0010\u0014\u00a8\u0006!"}, d2 = {"Lcom/example/androidproject/presentation/viewmodel/RehabViewModel;", "Landroidx/lifecycle/ViewModel;", "getAIRecommendationUseCase", "Lcom/example/androidproject/domain/usecase/GetAIRecommendationUseCase;", "(Lcom/example/androidproject/domain/usecase/GetAIRecommendationUseCase;)V", "_aiRecommendationResult", "Landroidx/lifecycle/MutableLiveData;", "Lcom/example/androidproject/domain/model/AIRecommendationResult;", "_errorMessage", "", "_isLoading", "", "_recommendedDiets", "", "Lcom/example/androidproject/domain/model/Diet;", "_recommendedExercises", "Lcom/example/androidproject/domain/model/Exercise;", "aiRecommendationResult", "Landroidx/lifecycle/LiveData;", "getAiRecommendationResult", "()Landroidx/lifecycle/LiveData;", "errorMessage", "getErrorMessage", "isLoading", "recommendedDiets", "getRecommendedDiets", "recommendedExercises", "getRecommendedExercises", "fetchAIRecommendations", "", "userId", "injury", "Lcom/example/androidproject/domain/model/Injury;", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel
public final class RehabViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull
    private final com.example.androidproject.domain.usecase.GetAIRecommendationUseCase getAIRecommendationUseCase = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.lifecycle.MutableLiveData<com.example.androidproject.domain.model.AIRecommendationResult> _aiRecommendationResult = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.lifecycle.MutableLiveData<java.util.List<com.example.androidproject.domain.model.Exercise>> _recommendedExercises = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.lifecycle.MutableLiveData<java.util.List<com.example.androidproject.domain.model.Diet>> _recommendedDiets = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.lifecycle.MutableLiveData<java.lang.Boolean> _isLoading = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.lifecycle.MutableLiveData<java.lang.String> _errorMessage = null;
    
    @javax.inject.Inject
    public RehabViewModel(@org.jetbrains.annotations.NotNull
    com.example.androidproject.domain.usecase.GetAIRecommendationUseCase getAIRecommendationUseCase) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final androidx.lifecycle.LiveData<com.example.androidproject.domain.model.AIRecommendationResult> getAiRecommendationResult() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final androidx.lifecycle.LiveData<java.util.List<com.example.androidproject.domain.model.Exercise>> getRecommendedExercises() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final androidx.lifecycle.LiveData<java.util.List<com.example.androidproject.domain.model.Diet>> getRecommendedDiets() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final androidx.lifecycle.LiveData<java.lang.Boolean> isLoading() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final androidx.lifecycle.LiveData<java.lang.String> getErrorMessage() {
        return null;
    }
    
    public final void fetchAIRecommendations(@org.jetbrains.annotations.NotNull
    java.lang.String userId, @org.jetbrains.annotations.Nullable
    com.example.androidproject.domain.model.Injury injury) {
    }
}