package com.example.androidproject.presentation.viewmodel;

import com.example.androidproject.domain.usecase.GetRecommendedRehabUseCase;
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
  private final Provider<GetRecommendedRehabUseCase> getRecommendedRehabUseCaseProvider;

  public RehabViewModel_Factory(
      Provider<GetRecommendedRehabUseCase> getRecommendedRehabUseCaseProvider) {
    this.getRecommendedRehabUseCaseProvider = getRecommendedRehabUseCaseProvider;
  }

  @Override
  public RehabViewModel get() {
    return newInstance(getRecommendedRehabUseCaseProvider.get());
  }

  public static RehabViewModel_Factory create(
      Provider<GetRecommendedRehabUseCase> getRecommendedRehabUseCaseProvider) {
    return new RehabViewModel_Factory(getRecommendedRehabUseCaseProvider);
  }

  public static RehabViewModel newInstance(GetRecommendedRehabUseCase getRecommendedRehabUseCase) {
    return new RehabViewModel(getRecommendedRehabUseCase);
  }
}
