package com.unibo.android.domain.usecase

import com.unibo.android.domain.model.Settings
import com.unibo.android.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class ObserveSettingsUseCase(private val repository: SettingsRepository) {
    operator fun invoke(): Flow<Settings?> = repository.observeSettings()
}
