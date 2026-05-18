package com.unibo.android.domain.usecase

import com.unibo.android.domain.model.Settings
import com.unibo.android.domain.repository.SettingsRepository

class GetSettingsUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(): Result<Settings> = repository.getSettings()
}
