package com.noodrop.app.ui.subscription;

import com.google.firebase.auth.FirebaseAuth;
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

  private final Provider<FirebaseAuth> authProvider;

  public SubscriptionViewModel_Factory(Provider<NoodropRepository> repoProvider,
      Provider<FirebaseAuth> authProvider) {
    this.repoProvider = repoProvider;
    this.authProvider = authProvider;
  }

  @Override
  public SubscriptionViewModel get() {
    return newInstance(repoProvider.get(), authProvider.get());
  }

  public static SubscriptionViewModel_Factory create(Provider<NoodropRepository> repoProvider,
      Provider<FirebaseAuth> authProvider) {
    return new SubscriptionViewModel_Factory(repoProvider, authProvider);
  }

  public static SubscriptionViewModel newInstance(NoodropRepository repo, FirebaseAuth auth) {
    return new SubscriptionViewModel(repo, auth);
  }
}
