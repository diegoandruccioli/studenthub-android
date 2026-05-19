package com.unibo.android.data.repository

import com.unibo.android.data.local.RankDataStore
import com.unibo.android.data.local.SessionDataStore
import com.unibo.android.data.remote.GamificationApiService
import com.unibo.android.domain.model.LeaderboardEntry
import com.unibo.android.domain.model.UserStats
import com.unibo.android.domain.repository.GamificationRepository
import com.unibo.android.domain.utils.GamificationUtils
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class GamificationRepositoryImpl(
    private val apiService: GamificationApiService,
    private val rankDataStore: RankDataStore,
    private val sessionDataStore: SessionDataStore,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : GamificationRepository {

    override suspend fun getLeaderboard(): Result<List<LeaderboardEntry>> = withContext(ioDispatcher) {
        runCatching {
            // Ottieni l'ID dell'utente loggato dal DataStore
            val currentUserId = getCurrentUserIdFromDataStore()
            
            val response = apiService.getLeaderboard()
            if (response.isSuccessful) {
                val body = response.body() ?: throw Exception("Empty body")
                body.leaderboard.mapIndexed { index, dto ->
                    LeaderboardEntry(
                        rank = index + 1,
                        userId = dto.id,
                        nome = dto.nome,
                        cognome = "",  // Il cognome non è ritornato dall'API nel DTO, usare fallback
                        xp = dto.xpTotali,
                        isCurrentUser = dto.id == currentUserId
                    )
                }
            } else {
                throw Exception("Error fetching leaderboard: ${response.code()}")
            }
        }
    }

    override suspend fun getUserStats(): Result<UserStats> = withContext(ioDispatcher) {
        runCatching {
            val response = apiService.getMyStatus()
            if (response.isSuccessful) {
                val body = response.body() ?: throw Exception("Empty body")
                
                // Calcola il level se non è ritornato dall'API
                val level = body.level ?: GamificationUtils.calculateLevel(body.xp)
                
                val stats = UserStats(
                    userId = body.userId,
                    nome = "",  // Il nome non è ritornato dall'API, recuperarlo altrove se necessario
                    cognome = "",
                    xp = body.xp,
                    level = level,
                    rank = body.rank
                )
                
                // Salva rank, XP e userId nel DataStore locale
                rankDataStore.saveRankAndXp(stats.rank, stats.xp)
                sessionDataStore.setUserId(stats.userId)  // Salva l'ID utente per getLeaderboard()
                
                stats
            } else {
                throw Exception("Error fetching stats: ${response.code()}")
            }
        }
    }

    /** da SessionDataStore.
     * Ritorna -1 se l'ID non è disponibile (l'utente non è ancora stato autenticato).
     */
    private suspend fun getCurrentUserIdFromDataStore(): Int {
        return try {
            sessionDataStore.userId.first()
        } catch (e: Exception) {
            -1
        }
        return -1  // TODO: Implementare un meccanismo affidabile per ottenere l'ID utente loggato
    }
}
