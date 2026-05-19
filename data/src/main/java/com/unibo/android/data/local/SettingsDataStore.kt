package com.unibo.android.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.unibo.android.domain.model.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    private val TEMA_VOTI       = stringPreferencesKey("tema_voti")
    private val SOGLIA_BASSA    = intPreferencesKey("rgb_soglia_bassa")
    private val SOGLIA_ALTA     = intPreferencesKey("rgb_soglia_alta")

    val settings: Flow<Settings?> = context.settingsDataStore.data.map { prefs ->
        val tema = prefs[TEMA_VOTI] ?: return@map null
        Settings(
            temaVoti       = tema,
            rgbSogliaBassa = prefs[SOGLIA_BASSA] ?: 18,
            rgbSogliaAlta  = prefs[SOGLIA_ALTA]  ?: 30
        )
    }

    suspend fun save(settings: Settings) {
        context.settingsDataStore.edit { prefs ->
            prefs[TEMA_VOTI]    = settings.temaVoti
            prefs[SOGLIA_BASSA] = settings.rgbSogliaBassa
            prefs[SOGLIA_ALTA]  = settings.rgbSogliaAlta
        }
    }
}
