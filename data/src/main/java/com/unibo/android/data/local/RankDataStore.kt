package com.unibo.android.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.rankDataStore: DataStore<Preferences> by preferencesDataStore(name = "rank_store")

class RankDataStore(private val context: Context) {
    private val CURRENT_RANK = intPreferencesKey("current_rank")
    private val CURRENT_XP = intPreferencesKey("current_xp")

    val currentRank: Flow<Int?> = context.rankDataStore.data.map { prefs -> prefs[CURRENT_RANK] }
    val currentXp: Flow<Int?> = context.rankDataStore.data.map { prefs -> prefs[CURRENT_XP] }

    suspend fun saveRankAndXp(rank: Int, xp: Int) {
        context.rankDataStore.edit { prefs ->
            prefs[CURRENT_RANK] = rank
            prefs[CURRENT_XP] = xp
        }
    }
}
