package com.noodrop.app.ui.dashboard;

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
public final class DashboardViewModel_Factory implements Factory<DashboardViewModel> {
  private final Provider<NoodropRepository> repoProvider;

  public DashboardViewModel_Factory(Provider<NoodropRepository> repoProvider) {
    this.repoProvider = repoProvider;
  }

  @Override
  public DashboardViewModel get() {
    return newInstance(repoProvider.get());
  }

  public static DashboardViewModel_Factory create(Provider<NoodropRepository> repoProvider) {
    return new DashboardViewModel_Factory(repoProvider);
  }

  public static DashboardViewModel newInstance(NoodropRepository repo) {
    return new DashboardViewModel(repo);
  }
}
