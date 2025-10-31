package com.example.androidproject.data.repository;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class AIApiRepositoryImpl_Factory implements Factory<AIApiRepositoryImpl> {
  @Override
  public AIApiRepositoryImpl get() {
    return newInstance();
  }

  public static AIApiRepositoryImpl_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static AIApiRepositoryImpl newInstance() {
    return new AIApiRepositoryImpl();
  }

  private static final class InstanceHolder {
    private static final AIApiRepositoryImpl_Factory INSTANCE = new AIApiRepositoryImpl_Factory();
  }
}
