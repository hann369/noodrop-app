package com.noodrop.app.ui.tracker;

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
public final class TrackerViewModel_Factory implements Factory<TrackerViewModel> {
  private final Provider<NoodropRepository> repoProvider;

  public TrackerViewModel_Factory(Provider<NoodropRepository> repoProvider) {
    this.repoProvider = repoProvider;
  }

  @Override
  public TrackerViewModel get() {
    return newInstance(repoProvider.get());
  }

  public static TrackerViewModel_Factory create(Provider<NoodropRepository> repoProvider) {
    return new TrackerViewModel_Factory(repoProvider);
  }

  public static TrackerViewModel newInstance(NoodropRepository repo) {
    return new TrackerViewModel(repo);
  }
}
