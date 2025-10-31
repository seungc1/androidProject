package com.example.androidproject.presentation.viewmodel;

import com.example.androidproject.domain.usecase.GetAIRecommendationUseCase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes"
})
public final class RehabViewModel_Factory implements Factory<RehabViewModel> {
  private final Provider<GetAIRecommendationUseCase> getAIRecommendationUseCaseProvider;

  public RehabViewModel_Factory(
      Provider<GetAIRecommendationUseCase> getAIRecommendationUseCaseProvider) {
    this.getAIRecommendationUseCaseProvider = getAIRecommendationUseCaseProvider;
  }

  @Override
  public RehabViewModel get() {
    return newInstance(getAIRecommendationUseCaseProvider.get());
  }

  public static RehabViewModel_Factory create(
      Provider<GetAIRecommendationUseCase> getAIRecommendationUseCaseProvider) {
    return new RehabViewModel_Factory(getAIRecommendationUseCaseProvider);
  }

  public static RehabViewModel newInstance(GetAIRecommendationUseCase getAIRecommendationUseCase) {
    return new RehabViewModel(getAIRecommendationUseCase);
  }
}
