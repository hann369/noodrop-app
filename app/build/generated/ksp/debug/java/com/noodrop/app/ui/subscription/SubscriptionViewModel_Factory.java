package com.noodrop.app.ui.subscription;

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
public final class SubscriptionViewModel_Factory implements Factory<SubscriptionViewModel> {
  private final Provider<NoodropRepository> repoProvider;

  public SubscriptionViewModel_Factory(Provider<NoodropRepository> repoProvider) {
    this.repoProvider = repoProvider;
  }

  @Override
  public SubscriptionViewModel get() {
    return newInstance(repoProvider.get());
  }

  public static SubscriptionViewModel_Factory create(Provider<NoodropRepository> repoProvider) {
    return new SubscriptionViewModel_Factory(repoProvider);
  }

  public static SubscriptionViewModel newInstance(NoodropRepository repo) {
    return new SubscriptionViewModel(repo);
  }
}
