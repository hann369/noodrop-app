package com.noodrop.app.ui.metrics;

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
public final class MetricsViewModel_Factory implements Factory<MetricsViewModel> {
  private final Provider<NoodropRepository> repoProvider;

  public MetricsViewModel_Factory(Provider<NoodropRepository> repoProvider) {
    this.repoProvider = repoProvider;
  }

  @Override
  public MetricsViewModel get() {
    return newInstance(repoProvider.get());
  }

  public static MetricsViewModel_Factory create(Provider<NoodropRepository> repoProvider) {
    return new MetricsViewModel_Factory(repoProvider);
  }

  public static MetricsViewModel newInstance(NoodropRepository repo) {
    return new MetricsViewModel(repo);
  }
}
