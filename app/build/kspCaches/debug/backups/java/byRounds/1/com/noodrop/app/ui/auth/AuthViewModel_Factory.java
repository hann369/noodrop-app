package com.noodrop.app.ui.auth;

import com.noodrop.app.data.repository.NoodropRepository;
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
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation"
})
public final class AuthViewModel_Factory implements Factory<AuthViewModel> {
  private final Provider<NoodropRepository> repoProvider;

  public AuthViewModel_Factory(Provider<NoodropRepository> repoProvider) {
    this.repoProvider = repoProvider;
  }

  @Override
  public AuthViewModel get() {
    return newInstance(repoProvider.get());
  }

  public static AuthViewModel_Factory create(Provider<NoodropRepository> repoProvider) {
    return new AuthViewModel_Factory(repoProvider);
  }

  public static AuthViewModel newInstance(NoodropRepository repo) {
    return new AuthViewModel(repo);
  }
}
