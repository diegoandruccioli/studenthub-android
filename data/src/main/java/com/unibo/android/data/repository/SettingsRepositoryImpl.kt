package com.unibo.android.data.repository

import android.content.Context
import com.unibo.android.data.local.SettingsDataStore
import com.unibo.android.data.remote.NetworkClient
import com.unibo.android.data.remote.dto.SettingsRequest
import com.unibo.android.domain.model.Settings
import com.unibo.android.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class SettingsRepositoryImpl(context: Context) : SettingsRepository {

    private val api = NetworkClient.settingsApiService
    private val dataStore = SettingsDataStore(context)

    override suspend fun getSettings(): Result<Settings> = withContext(Dispatchers.IO) {
        val apiResult = runCatching {
            val response = api.getSettings()
            if (response.isSuccessful) {
                val body = response.body()!!
                Settings(
                    temaVoti       = body.temaVoti,
                    rgbSogliaBassa = body.rgbSogliaBassa,
                    rgbSogliaAlta  = body.rgbSogliaAlta
                ).also { dataStore.save(it) }
            } else {
                throw Exception("Impossibile caricare le impostazioni")
            }
        }

        if (apiResult.isSuccess) return@withContext apiResult

        val cached = dataStore.settings.first()
        if (cached != null) Result.success(cached)
        else Result.failure(apiResult.exceptionOrNull() ?: Exception("Impostazioni non disponibili"))
    }

    override suspend fun updateSettings(settings: Settings): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.updateSettings(
                SettingsRequest(
                    temaVoti       = settings.temaVoti,
                    rgbSogliaBassa = settings.rgbSogliaBassa,
                    rgbSogliaAlta  = settings.rgbSogliaAlta
                )
            )
            if (!response.isSuccessful) throw Exception("Salvataggio impostazioni fallito")
            dataStore.save(settings)
        }
    }
}
