package com.example.androidproject.domain.usecase;

import com.example.androidproject.domain.repository.RehabRepository;
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
public final class GetRecommendedRehabUseCase_Factory implements Factory<GetRecommendedRehabUseCase> {
  private final Provider<RehabRepository> repositoryProvider;

  public GetRecommendedRehabUseCase_Factory(Provider<RehabRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public GetRecommendedRehabUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static GetRecommendedRehabUseCase_Factory create(
      Provider<RehabRepository> repositoryProvider) {
    return new GetRecommendedRehabUseCase_Factory(repositoryProvider);
  }

  public static GetRecommendedRehabUseCase newInstance(RehabRepository repository) {
    return new GetRecommendedRehabUseCase(repository);
  }
}
