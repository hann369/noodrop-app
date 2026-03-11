package com.noodrop.app.data.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class FirebaseDataSource_Factory implements Factory<FirebaseDataSource> {
  private final Provider<FirebaseAuth> authProvider;

  private final Provider<FirebaseFirestore> dbProvider;

  public FirebaseDataSource_Factory(Provider<FirebaseAuth> authProvider,
      Provider<FirebaseFirestore> dbProvider) {
    this.authProvider = authProvider;
    this.dbProvider = dbProvider;
  }

  @Override
  public FirebaseDataSource get() {
    return newInstance(authProvider.get(), dbProvider.get());
  }

  public static FirebaseDataSource_Factory create(Provider<FirebaseAuth> authProvider,
      Provider<FirebaseFirestore> dbProvider) {
    return new FirebaseDataSource_Factory(authProvider, dbProvider);
  }

  public static FirebaseDataSource newInstance(FirebaseAuth auth, FirebaseFirestore db) {
    return new FirebaseDataSource(auth, db);
  }
}
