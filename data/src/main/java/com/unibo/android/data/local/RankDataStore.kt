package com.unibo.android.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.unibo.android.data.remote.dto.GamificationStatusDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.rankDataStore: DataStore<Preferences> by preferencesDataStore(name = "rank")

class RankDataStore(private val context: Context) {

    private val CURRENT_XP = intPreferencesKey("current_xp")
    private val CURRENT_RANK = intPreferencesKey("current_rank")
    private val CURRENT_LEVEL = intPreferencesKey("current_level")
    private val LEVEL_TITLE = stringPreferencesKey("level_title")
    private val PROGRESS_PERCENTAGE = floatPreferencesKey("progress_percentage")
    private val PROSSIMA_SOGLIA = intPreferencesKey("prossima_soglia")
    private val LAST_CHECK_TIMESTAMP = longPreferencesKey("last_check_timestamp")

    val currentXp: Flow<Int> = context.rankDataStore.data.map { it[CURRENT_XP] ?: 0 }
    val currentRank: Flow<Int> = context.rankDataStore.data.map { it[CURRENT_RANK] ?: 0 }
    val currentLevel: Flow<Int> = context.rankDataStore.data.map { it[CURRENT_LEVEL] ?: 1 }
    val levelTitle: Flow<String> = context.rankDataStore.data.map { it[LEVEL_TITLE] ?: "" }
    val progressPercentage: Flow<Float> = context.rankDataStore.data.map { it[PROGRESS_PERCENTAGE] ?: 0f }
    val prossimaSoglia: Flow<Int?> = context.rankDataStore.data.map { it[PROSSIMA_SOGLIA] }
    val lastCheckTimestamp: Flow<Long> = context.rankDataStore.data.map { it[LAST_CHECK_TIMESTAMP] ?: 0L }

    suspend fun saveStatus(dto: GamificationStatusDto) {
        context.rankDataStore.edit { prefs ->
            prefs[CURRENT_XP] = dto.xpTotali
            prefs[CURRENT_LEVEL] = dto.livello.numero
            prefs[LEVEL_TITLE] = dto.livello.nome
            prefs[PROGRESS_PERCENTAGE] = dto.progress.percentuale.toFloat()
            dto.progress.prossimaSoglia?.let { prefs[PROSSIMA_SOGLIA] = it }
        }
    }

    suspend fun saveMyRank(rank: Int) {
        context.rankDataStore.edit { prefs ->
            prefs[CURRENT_RANK] = rank
        }
    }

    suspend fun saveLastCheckTimestamp(timestamp: Long) {
        context.rankDataStore.edit { prefs ->
            prefs[LAST_CHECK_TIMESTAMP] = timestamp
        }
    }

    suspend fun clear() {
        context.rankDataStore.edit { it.clear() }
    }
}
