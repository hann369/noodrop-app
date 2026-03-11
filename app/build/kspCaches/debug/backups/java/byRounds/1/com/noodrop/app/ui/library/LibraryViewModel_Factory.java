package com.noodrop.app.ui.library;

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
public final class LibraryViewModel_Factory implements Factory<LibraryViewModel> {
  private final Provider<NoodropRepository> repoProvider;

  public LibraryViewModel_Factory(Provider<NoodropRepository> repoProvider) {
    this.repoProvider = repoProvider;
  }

  @Override
  public LibraryViewModel get() {
    return newInstance(repoProvider.get());
  }

  public static LibraryViewModel_Factory create(Provider<NoodropRepository> repoProvider) {
    return new LibraryViewModel_Factory(repoProvider);
  }

  public static LibraryViewModel newInstance(NoodropRepository repo) {
    return new LibraryViewModel(repo);
  }
}
