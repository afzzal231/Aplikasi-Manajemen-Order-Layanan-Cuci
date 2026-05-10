package com.afzzal0039.aplikasimanajemenorderlayanancuci.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    private val IS_GRID_LAYOUT = booleanPreferencesKey("is_grid_layout")

    private val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")

    val isGridLayout: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_GRID_LAYOUT] ?: false
        }

    val isDarkMode: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_DARK_MODE] ?: false
        }

    suspend fun saveLayoutSetting(isGrid: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_GRID_LAYOUT] = isGrid
        }
    }

    suspend fun saveDarkMode(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_DARK_MODE] = isDark
        }
    }
}