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
public final class FakeUserRepository_Factory implements Factory<FakeUserRepository> {
  @Override
  public FakeUserRepository get() {
    return newInstance();
  }

  public static FakeUserRepository_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static FakeUserRepository newInstance() {
    return new FakeUserRepository();
  }

  private static final class InstanceHolder {
    private static final FakeUserRepository_Factory INSTANCE = new FakeUserRepository_Factory();
  }
}
