package com.example.androidproject.data.repository;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class RehabSessionRepositoryImpl_Factory implements Factory<RehabSessionRepositoryImpl> {
  @Override
  public RehabSessionRepositoryImpl get() {
    return newInstance();
  }

  public static RehabSessionRepositoryImpl_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static RehabSessionRepositoryImpl newInstance() {
    return new RehabSessionRepositoryImpl();
  }

  private static final class InstanceHolder {
    private static final RehabSessionRepositoryImpl_Factory INSTANCE = new RehabSessionRepositoryImpl_Factory();
  }
}
