package com.noodrop.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "noodrop_prefs")

@Singleton
class ThemePreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private val KEY_DARK          = booleanPreferencesKey("dark_mode")
        private val KEY_ONBOARDING    = booleanPreferencesKey("onboarding_done")
    }

    val isDarkFlow: Flow<Boolean> = context.dataStore.data
        .map { it[KEY_DARK] ?: false }

    val onboardingDoneFlow: Flow<Boolean> = context.dataStore.data
        .map { it[KEY_ONBOARDING] ?: false }

    suspend fun setDark(dark: Boolean) {
        context.dataStore.edit { it[KEY_DARK] = dark }
    }

    suspend fun setOnboardingDone(done: Boolean) {
        context.dataStore.edit { it[KEY_ONBOARDING] = done }
    }
}
