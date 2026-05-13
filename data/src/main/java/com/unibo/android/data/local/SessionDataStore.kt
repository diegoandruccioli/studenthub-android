package com.unibo.android.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

class SessionDataStore(private val context: Context) {

    private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")

    val isLoggedIn: Flow<Boolean> = context.sessionDataStore.data
        .map { prefs -> prefs[IS_LOGGED_IN] ?: false }

    suspend fun setLoggedIn(value: Boolean) {
        context.sessionDataStore.edit { prefs -> prefs[IS_LOGGED_IN] = value }
    }
}
