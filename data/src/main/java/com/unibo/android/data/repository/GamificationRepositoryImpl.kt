package com.unibo.android.data.repository

import com.unibo.android.data.local.RankDataStore
import com.unibo.android.data.remote.LeaderboardApiService
import com.unibo.android.domain.model.LeaderboardEntry
import com.unibo.android.domain.model.UserStats
import com.unibo.android.domain.repository.GamificationRepository
import com.unibo.android.domain.utils.GamificationUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GamificationRepositoryImpl(
    private val apiService: LeaderboardApiService?,
    private val rankDataStore: RankDataStore
) : GamificationRepository {

    // Simuliamo l'ID dell'utente corrente (es. Rei, basandoci sul mockup)
    private val currentUserId = 4

    override suspend fun getLeaderboard(): Result<List<LeaderboardEntry>> = withContext(Dispatchers.IO) {
        try {
            // Proviamo a chiamare l'API vera
            val dtos = apiService?.getLeaderboard() ?: throw Exception("ApiService is null")
            val entries = dtos.map { dto ->
                LeaderboardEntry(
                    rank = dto.rank,
                    userId = dto.userId,
                    nome = dto.nome,
                    cognome = dto.cognome,
                    xp = dto.xp,
                    isCurrentUser = dto.userId == currentUserId
                )
            }
            Result.success(entries)
        } catch (e: Exception) {
            // Fallback: mock data per UI testing se l'API fallisce
            val mockData = listOf(
                LeaderboardEntry(1, 1, "Diego", "", 2682, false),
                LeaderboardEntry(2, 2, "Giovanni", "", 2362, false),
                LeaderboardEntry(3, 3, "Rei", "", 1796, false),
                LeaderboardEntry(4, currentUserId, "Rei", "", 330, true)
            )
            Result.success(mockData)
        }
    }

    override suspend fun getUserStats(): Result<UserStats> = withContext(Dispatchers.IO) {
        try {
            val leaderboardResult = getLeaderboard()
            if (leaderboardResult.isSuccess) {
                val list = leaderboardResult.getOrNull() ?: emptyList()
                val currentUserEntry = list.find { it.userId == currentUserId }
                
                if (currentUserEntry != null) {
                    val stats = UserStats(
                        userId = currentUserEntry.userId,
                        nome = currentUserEntry.nome,
                        cognome = currentUserEntry.cognome,
                        xp = currentUserEntry.xp,
                        level = GamificationUtils.calculateLevel(currentUserEntry.xp),
                        rank = currentUserEntry.rank
                    )
                    // Salvataggio nel DataStore per uso offline/worker
                    rankDataStore.saveRankAndXp(stats.rank, stats.xp)
                    return@withContext Result.success(stats)
                }
            }
            // Fallback utente non trovato
            Result.success(UserStats(currentUserId, "Rei", "", 330, GamificationUtils.calculateLevel(330), 4))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
