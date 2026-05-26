package com.unibo.android.data.repository

import android.content.Context
import android.util.Log
import com.unibo.android.data.local.SettingsDataStore
import com.unibo.android.data.remote.NetworkClient
import com.unibo.android.data.remote.dto.SettingsRequest
import com.unibo.android.domain.model.Settings
import com.unibo.android.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

private const val TAG = "SettingsRepository"

class SettingsRepositoryImpl(context: Context) : SettingsRepository {

    private val api = NetworkClient.settingsApiService
    private val dataStore = SettingsDataStore(context)

    override suspend fun getSettings(): Result<Settings> = withContext(Dispatchers.IO) {
        Log.d(TAG, "getSettings → GET /settings...")
        val apiResult = runCatching {
            val response = api.getSettings()
            if (response.isSuccessful) {
                val body = response.body()!!
                Log.d(TAG, "getSettings → server OK, tema=${body.temaVoti}")
                Settings(
                    temaVoti       = body.temaVoti,
                    rgbSogliaBassa = body.rgbSogliaBassa,
                    rgbSogliaAlta  = body.rgbSogliaAlta
                ).also { dataStore.save(it) }
            } else {
                throw Exception("getSettings HTTP ${response.code()}")
            }
        }.onFailure { e ->
            Log.w(TAG, "getSettings → server fallito, uso cache: ${e.message}")
        }

        if (apiResult.isSuccess) return@withContext apiResult

        val cached = dataStore.settings.first()
        return@withContext if (cached != null) {
            Log.d(TAG, "getSettings → cache DataStore: tema=${cached.temaVoti}")
            Result.success(cached)
        } else {
            Log.e(TAG, "getSettings → nessuna cache disponibile")
            Result.failure(apiResult.exceptionOrNull() ?: Exception("Impostazioni non disponibili"))
        }
    }

    override suspend fun updateSettings(settings: Settings): Result<Unit> = withContext(Dispatchers.IO) {
        // DataStore è fonte di verità locale: salvare prima dell'API
        val localSave = runCatching { dataStore.save(settings) }
        if (localSave.isFailure) return@withContext Result.failure(localSave.exceptionOrNull()!!)

        // Sync server: errore ignorato (DataStore già aggiornato)
        Log.d(TAG, "updateSettings → PUT /settings tema=${settings.temaVoti}...")
        runCatching {
            val response = api.updateSettings(
                SettingsRequest(
                    temaVoti       = settings.temaVoti,
                    rgbSogliaBassa = settings.rgbSogliaBassa,
                    rgbSogliaAlta  = settings.rgbSogliaAlta
                )
            )
            response.body()?.close()
            if (response.isSuccessful) {
                Log.d(TAG, "updateSettings → server OK")
            } else {
                Log.w(TAG, "updateSettings → server HTTP ${response.code()} (DataStore già aggiornato)")
            }
        }.onFailure { e ->
            Log.w(TAG, "updateSettings → server non raggiungibile (DataStore già aggiornato): ${e.message}")
        }

        Result.success(Unit)
    }

    override fun observeSettings(): Flow<Settings?> = dataStore.settings
}
