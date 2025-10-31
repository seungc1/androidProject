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
public final class DietSessionRepositoryImpl_Factory implements Factory<DietSessionRepositoryImpl> {
  @Override
  public DietSessionRepositoryImpl get() {
    return newInstance();
  }

  public static DietSessionRepositoryImpl_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static DietSessionRepositoryImpl newInstance() {
    return new DietSessionRepositoryImpl();
  }

  private static final class InstanceHolder {
    private static final DietSessionRepositoryImpl_Factory INSTANCE = new DietSessionRepositoryImpl_Factory();
  }
}
