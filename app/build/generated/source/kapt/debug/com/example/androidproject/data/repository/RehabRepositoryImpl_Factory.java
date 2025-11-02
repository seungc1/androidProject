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
public final class RehabRepositoryImpl_Factory implements Factory<RehabRepositoryImpl> {
  @Override
  public RehabRepositoryImpl get() {
    return newInstance();
  }

  public static RehabRepositoryImpl_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static RehabRepositoryImpl newInstance() {
    return new RehabRepositoryImpl();
  }

  private static final class InstanceHolder {
    private static final RehabRepositoryImpl_Factory INSTANCE = new RehabRepositoryImpl_Factory();
  }
}
