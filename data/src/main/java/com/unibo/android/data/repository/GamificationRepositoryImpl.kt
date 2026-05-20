package com.unibo.android.data.repository

import android.content.Context
import com.unibo.android.data.local.RankDataStore
import com.unibo.android.data.local.SessionDataStore
import com.unibo.android.data.local.StudentHubDatabase
import com.unibo.android.data.local.entity.LeaderboardEntity
import com.unibo.android.data.remote.NetworkClient
import com.unibo.android.domain.model.LeaderboardEntry
import com.unibo.android.domain.model.UserStats
import com.unibo.android.domain.repository.GamificationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class GamificationRepositoryImpl(context: Context) : GamificationRepository {

    private val api = NetworkClient.gamificationApiService
    private val rankDataStore = RankDataStore(context)
    private val sessionDataStore = SessionDataStore(context)
    private val leaderboardDao = StudentHubDatabase.getInstance(context).leaderboardDao()

    override val userStatsFlow: Flow<UserStats> = combine(
        rankDataStore.currentXp,
        rankDataStore.currentRank,
        rankDataStore.currentLevel,
        rankDataStore.levelTitle,
        rankDataStore.progressPercentage,
    ) { params: Array<Any> ->
        UserStats(
            userId = 0, // Verrà popolato correttamente via GetGamificationDataUseCase
            xp = params[0] as Int,
            rank = params[1] as Int,
            level = params[2] as Int,
            levelTitle = params[3] as String,
            progressPercentage = params[4] as Float,
            xpLabel = "", // Verrà calcolato via GetGamificationDataUseCase
        )
    }

    /**
     * Leaderboard flow observed directly from the local Room database (SSOT).
     */
    override val leaderboardFlow: Flow<List<LeaderboardEntry>> = leaderboardDao.getLeaderboard()
        .map { entities ->
            entities.map {
                LeaderboardEntry(
                    userId = it.userId,
                    nome = it.nome,
                    cognome = it.cognome,
                    xpTotali = it.xpTotali,
                )
            }
        }

    override suspend fun getUserStats(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.getStatus()
            if (response.isSuccessful) {
                response.body()?.let { rankDataStore.saveStatus(it) }
                Result.success(Unit)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Errore di rete: controlla la connessione internet", e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLeaderboard(): Result<List<LeaderboardEntry>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getLeaderboard()
            if (response.isSuccessful) {
                val entriesDto = response.body() ?: emptyList()
                
                // Mappatura DTO -> Entity per salvataggio locale (SSOT)
                val entities = entriesDto.map {
                    LeaderboardEntity(
                        userId = it.userId,
                        nome = it.nome,
                        cognome = it.cognome,
                        xpTotali = it.xpTotali,
                    )
                }
                leaderboardDao.refreshLeaderboard(entities)

                // Ritorna la lista mappata per compatibilità immediata se necessario, 
                // sebbene la UI debba osservare leaderboardFlow.
                val domainEntries = entriesDto.map {
                    LeaderboardEntry(
                        userId = it.userId,
                        nome = it.nome,
                        cognome = it.cognome,
                        xpTotali = it.xpTotali,
                    )
                }
                Result.success(domainEntries)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Errore di rete: impossibile scaricare la classifica", e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
