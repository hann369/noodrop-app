package com.noodrop.app.ui.profile;

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
public final class ProfileViewModel_Factory implements Factory<ProfileViewModel> {
  private final Provider<NoodropRepository> repoProvider;

  public ProfileViewModel_Factory(Provider<NoodropRepository> repoProvider) {
    this.repoProvider = repoProvider;
  }

  @Override
  public ProfileViewModel get() {
    return newInstance(repoProvider.get());
  }

  public static ProfileViewModel_Factory create(Provider<NoodropRepository> repoProvider) {
    return new ProfileViewModel_Factory(repoProvider);
  }

  public static ProfileViewModel newInstance(NoodropRepository repo) {
    return new ProfileViewModel(repo);
  }
}
