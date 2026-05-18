package com.unibo.android.domain.repository

import com.unibo.android.domain.model.Settings

interface SettingsRepository {
    suspend fun getSettings(): Result<Settings>
    suspend fun updateSettings(settings: Settings): Result<Unit>
}
