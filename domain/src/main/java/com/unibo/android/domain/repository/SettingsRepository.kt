package com.unibo.android.domain.repository

import com.unibo.android.domain.model.Settings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    suspend fun getSettings(): Result<Settings>
    suspend fun updateSettings(settings: Settings): Result<Unit>
    fun observeSettings(): Flow<Settings?>
    fun observeLastCheckTimestamp(): Flow<Long>
    suspend fun runLeaderboardWorkerNow(): Result<Unit>
    suspend fun triggerTestNotification(): Result<Unit>
    fun observeLocalRank(): Flow<Int>
    suspend fun setLocalRank(rank: Int): Result<Unit>
}
