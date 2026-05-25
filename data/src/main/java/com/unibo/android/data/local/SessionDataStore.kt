package com.unibo.android.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

class SessionDataStore(private val context: Context) {

    private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    private val USER_ID = intPreferencesKey("user_id")

    val isLoggedIn: Flow<Boolean> = context.sessionDataStore.data
        .map { prefs -> prefs[IS_LOGGED_IN] ?: false }

    val userId: Flow<Int> = context.sessionDataStore.data
        .map { prefs -> prefs[USER_ID] ?: 0 }

    suspend fun setLoggedIn(value: Boolean) {
        context.sessionDataStore.edit { prefs -> prefs[IS_LOGGED_IN] = value }
    }

    suspend fun setUserId(value: Int) {
        context.sessionDataStore.edit { prefs -> prefs[USER_ID] = value }
    }
}
