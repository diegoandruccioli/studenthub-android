package com.unibo.android.data.repository

import com.unibo.android.data.remote.NetworkClient
import com.unibo.android.data.remote.dto.SettingsRequest
import com.unibo.android.domain.model.Settings
import com.unibo.android.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SettingsRepositoryImpl : SettingsRepository {

    private val api = NetworkClient.settingsApiService

    override suspend fun getSettings(): Result<Settings> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getSettings()
            if (response.isSuccessful) {
                val body = response.body()!!
                Settings(
                    temaVoti = body.temaVoti,
                    rgbSogliaBassa = body.rgbSogliaBassa,
                    rgbSogliaAlta = body.rgbSogliaAlta
                )
            } else {
                throw Exception("Impossibile caricare le impostazioni")
            }
        }
    }

    override suspend fun updateSettings(settings: Settings): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.updateSettings(
                SettingsRequest(
                    temaVoti = settings.temaVoti,
                    rgbSogliaBassa = settings.rgbSogliaBassa,
                    rgbSogliaAlta = settings.rgbSogliaAlta
                )
            )
            if (!response.isSuccessful) throw Exception("Salvataggio impostazioni fallito")
        }
    }
}
