package com.unibo.android.data.repository

import android.content.Context
import com.unibo.android.data.local.RankDataStore
import com.unibo.android.data.local.SessionDataStore
import com.unibo.android.data.remote.NetworkClient
import com.unibo.android.domain.model.LeaderboardEntry
import com.unibo.android.domain.model.UserStats
import com.unibo.android.domain.repository.GamificationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext

class GamificationRepositoryImpl(context: Context) : GamificationRepository {

    private val api = NetworkClient.gamificationApiService
    private val rankDataStore = RankDataStore(context)
    private val sessionDataStore = SessionDataStore(context)

    override val userStatsFlow: Flow<UserStats> = combine(
        sessionDataStore.userId,
        rankDataStore.currentXp,
        rankDataStore.currentRank,
        rankDataStore.currentLevel,
        rankDataStore.levelTitle,
        rankDataStore.progressPercentage
    ) { params: Array<Any> ->
        UserStats(
            userId = params[0] as Int,
            xp = params[1] as Int,
            rank = params[2] as Int,
            level = params[3] as Int,
            levelTitle = params[4] as String,
            progressPercentage = params[5] as Float
        )
    }

    override suspend fun getUserStats(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.getStatus()
            if (response.isSuccessful) {
                response.body()?.let { rankDataStore.saveStatus(it) }
                Result.success(Unit)
            } else {
                Result.failure(Exception("Errore nel recupero dello stato gamification"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLeaderboard(): Result<List<LeaderboardEntry>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getLeaderboard()
            if (response.isSuccessful) {
                val entries = response.body()?.map {
                    LeaderboardEntry(
                        userId = it.userId,
                        nome = it.nome,
                        cognome = it.cognome,
                        xpTotali = it.xpTotali
                    )
                } ?: emptyList()
                Result.success(entries)
            } else {
                Result.failure(Exception("Errore nel recupero della classifica"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
