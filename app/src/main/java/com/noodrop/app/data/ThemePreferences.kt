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

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("noodrop_prefs")

@Singleton
class ThemePreferences @Inject constructor(
    @ApplicationContext private val ctx: Context,
) {
    private val DARK_MODE = booleanPreferencesKey("dark_mode")

    val isDarkFlow: Flow<Boolean> = ctx.dataStore.data
        .map { it[DARK_MODE] ?: false }

    suspend fun setDark(dark: Boolean) {
        ctx.dataStore.edit { it[DARK_MODE] = dark }
    }
}
