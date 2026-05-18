package com.unibo.android.domain.usecase

import com.unibo.android.domain.model.Settings
import com.unibo.android.domain.repository.SettingsRepository

class UpdateSettingsUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(settings: Settings): Result<Unit> = repository.updateSettings(settings)
}
