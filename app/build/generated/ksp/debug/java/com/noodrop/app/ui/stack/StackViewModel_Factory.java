package com.noodrop.app.ui.stack;

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
public final class StackViewModel_Factory implements Factory<StackViewModel> {
  private final Provider<NoodropRepository> repoProvider;

  public StackViewModel_Factory(Provider<NoodropRepository> repoProvider) {
    this.repoProvider = repoProvider;
  }

  @Override
  public StackViewModel get() {
    return newInstance(repoProvider.get());
  }

  public static StackViewModel_Factory create(Provider<NoodropRepository> repoProvider) {
    return new StackViewModel_Factory(repoProvider);
  }

  public static StackViewModel newInstance(NoodropRepository repo) {
    return new StackViewModel(repo);
  }
}
