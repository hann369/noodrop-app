package com.noodrop.app;

import com.noodrop.app.data.ThemePreferences;
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
public final class RootViewModel_Factory implements Factory<RootViewModel> {
  private final Provider<NoodropRepository> repoProvider;

  private final Provider<ThemePreferences> prefsProvider;

  public RootViewModel_Factory(Provider<NoodropRepository> repoProvider,
      Provider<ThemePreferences> prefsProvider) {
    this.repoProvider = repoProvider;
    this.prefsProvider = prefsProvider;
  }

  @Override
  public RootViewModel get() {
    return newInstance(repoProvider.get(), prefsProvider.get());
  }

  public static RootViewModel_Factory create(Provider<NoodropRepository> repoProvider,
      Provider<ThemePreferences> prefsProvider) {
    return new RootViewModel_Factory(repoProvider, prefsProvider);
  }

  public static RootViewModel newInstance(NoodropRepository repo, ThemePreferences prefs) {
    return new RootViewModel(repo, prefs);
  }
}
