package com.example.androidproject.domain.usecase;

import com.example.androidproject.domain.repository.DietSessionRepository;
import com.example.androidproject.domain.repository.RehabSessionRepository;
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
public final class AnalyzeRehabProgressUseCase_Factory implements Factory<AnalyzeRehabProgressUseCase> {
  private final Provider<RehabSessionRepository> rehabSessionRepositoryProvider;

  private final Provider<DietSessionRepository> dietSessionRepositoryProvider;

  public AnalyzeRehabProgressUseCase_Factory(
      Provider<RehabSessionRepository> rehabSessionRepositoryProvider,
      Provider<DietSessionRepository> dietSessionRepositoryProvider) {
    this.rehabSessionRepositoryProvider = rehabSessionRepositoryProvider;
    this.dietSessionRepositoryProvider = dietSessionRepositoryProvider;
  }

  @Override
  public AnalyzeRehabProgressUseCase get() {
    return newInstance(rehabSessionRepositoryProvider.get(), dietSessionRepositoryProvider.get());
  }

  public static AnalyzeRehabProgressUseCase_Factory create(
      Provider<RehabSessionRepository> rehabSessionRepositoryProvider,
      Provider<DietSessionRepository> dietSessionRepositoryProvider) {
    return new AnalyzeRehabProgressUseCase_Factory(rehabSessionRepositoryProvider, dietSessionRepositoryProvider);
  }

  public static AnalyzeRehabProgressUseCase newInstance(
      RehabSessionRepository rehabSessionRepository, DietSessionRepository dietSessionRepository) {
    return new AnalyzeRehabProgressUseCase(rehabSessionRepository, dietSessionRepository);
  }
}
