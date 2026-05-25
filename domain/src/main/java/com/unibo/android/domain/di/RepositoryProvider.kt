package com.unibo.android.domain.di

import com.unibo.android.domain.repository.AuthRepository
import com.unibo.android.domain.repository.EsameRepository
import com.unibo.android.domain.repository.GamificationRepository
import com.unibo.android.domain.repository.ObiettivoRepository
import com.unibo.android.domain.repository.SettingsRepository

interface RepositoryProvider {
    fun getEsameRepository(): EsameRepository
    fun getObiettivoRepository(): ObiettivoRepository
    fun getAuthRepository(): AuthRepository
    fun getSettingsRepository(): SettingsRepository
    fun getGamificationRepository(): GamificationRepository
}
