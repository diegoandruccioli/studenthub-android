package com.unibo.android.domain.repository

import com.unibo.android.domain.model.Settings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    suspend fun getSettings(): Result<Settings>
    suspend fun updateSettings(settings: Settings): Result<Unit>
    fun observeSettings(): Flow<Settings?>
}
