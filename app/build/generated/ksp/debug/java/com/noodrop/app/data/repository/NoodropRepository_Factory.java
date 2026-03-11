package com.noodrop.app.data.repository;

import com.noodrop.app.data.firebase.FirebaseDataSource;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation"
})
public final class NoodropRepository_Factory implements Factory<NoodropRepository> {
  private final Provider<FirebaseDataSource> fbProvider;

  public NoodropRepository_Factory(Provider<FirebaseDataSource> fbProvider) {
    this.fbProvider = fbProvider;
  }

  @Override
  public NoodropRepository get() {
    return newInstance(fbProvider.get());
  }

  public static NoodropRepository_Factory create(Provider<FirebaseDataSource> fbProvider) {
    return new NoodropRepository_Factory(fbProvider);
  }

  public static NoodropRepository newInstance(FirebaseDataSource fb) {
    return new NoodropRepository(fb);
  }
}
