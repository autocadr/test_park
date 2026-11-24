package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.parkFinderDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "park_finder_prefs",
)

class UserPreferencesRepository(private val context: Context) {
    private val preferredMapKey = stringPreferencesKey("preferred_map_app")

    val preferredMapApp: Flow<PreferredMapApp> = context.parkFinderDataStore.data.map { prefs ->
        PreferredMapApp.fromName(prefs[preferredMapKey])
    }

    suspend fun setPreferredMapApp(app: PreferredMapApp) {
        context.parkFinderDataStore.edit { prefs ->
            prefs[preferredMapKey] = app.name
        }
    }
}
