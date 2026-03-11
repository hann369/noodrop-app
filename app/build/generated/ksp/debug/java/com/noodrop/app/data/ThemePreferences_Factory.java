package com.noodrop.app.data;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class ThemePreferences_Factory implements Factory<ThemePreferences> {
  private final Provider<Context> ctxProvider;

  public ThemePreferences_Factory(Provider<Context> ctxProvider) {
    this.ctxProvider = ctxProvider;
  }

  @Override
  public ThemePreferences get() {
    return newInstance(ctxProvider.get());
  }

  public static ThemePreferences_Factory create(Provider<Context> ctxProvider) {
    return new ThemePreferences_Factory(ctxProvider);
  }

  public static ThemePreferences newInstance(Context ctx) {
    return new ThemePreferences(ctx);
  }
}
