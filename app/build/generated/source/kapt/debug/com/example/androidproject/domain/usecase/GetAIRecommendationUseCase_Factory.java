package com.example.androidproject.domain.usecase;

import com.example.androidproject.domain.repository.AIApiRepository;
import com.example.androidproject.domain.repository.UserRepository;
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
public final class GetAIRecommendationUseCase_Factory implements Factory<GetAIRecommendationUseCase> {
  private final Provider<AIApiRepository> aiApiRepositoryProvider;

  private final Provider<UserRepository> userRepositoryProvider;

  public GetAIRecommendationUseCase_Factory(Provider<AIApiRepository> aiApiRepositoryProvider,
      Provider<UserRepository> userRepositoryProvider) {
    this.aiApiRepositoryProvider = aiApiRepositoryProvider;
    this.userRepositoryProvider = userRepositoryProvider;
  }

  @Override
  public GetAIRecommendationUseCase get() {
    return newInstance(aiApiRepositoryProvider.get(), userRepositoryProvider.get());
  }

  public static GetAIRecommendationUseCase_Factory create(
      Provider<AIApiRepository> aiApiRepositoryProvider,
      Provider<UserRepository> userRepositoryProvider) {
    return new GetAIRecommendationUseCase_Factory(aiApiRepositoryProvider, userRepositoryProvider);
  }

  public static GetAIRecommendationUseCase newInstance(AIApiRepository aiApiRepository,
      UserRepository userRepository) {
    return new GetAIRecommendationUseCase(aiApiRepository, userRepository);
  }
}
