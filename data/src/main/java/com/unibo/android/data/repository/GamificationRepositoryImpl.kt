package com.unibo.android.data.repository

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.unibo.android.data.local.RankDataStore
import com.unibo.android.data.local.SessionDataStore
import com.unibo.android.data.local.StudentHubDatabase
import com.unibo.android.data.local.entity.LeaderboardEntity
import com.unibo.android.data.remote.NetworkClient
import com.unibo.android.data.worker.LeaderboardCheckWorker
import com.unibo.android.data.worker.NotificationUtils
import com.unibo.android.domain.model.LeaderboardEntry
import com.unibo.android.domain.model.UserStats
import com.unibo.android.domain.repository.GamificationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class GamificationRepositoryImpl(private val context: Context) : GamificationRepository {
    private val api = NetworkClient.gamificationApiService
    private val rankDataStore = RankDataStore(context)
    private val sessionDataStore = SessionDataStore(context)
    private val leaderboardDao = StudentHubDatabase.getInstance(context).leaderboardDao()

    override val userStatsFlow: Flow<UserStats> =
        combine(
            rankDataStore.currentXp,
            rankDataStore.currentRank,
            rankDataStore.currentLevel,
            rankDataStore.levelTitle,
            rankDataStore.progressPercentage,
            sessionDataStore.userId,
            rankDataStore.prossimaSoglia,
        ) { params: Array<Any?> ->
            UserStats(
                userId = params[5] as Int,
                xp = params[0] as Int,
                rank = params[1] as Int,
                level = params[2] as Int,
                levelTitle = params[3] as String,
                progressPercentage = params[4] as Float,
                xpLabel = "", // calcolato via GetGamificationDataUseCase
                prossimaSoglia = params[6] as Int?,
            )
        }

    override val leaderboardFlow: Flow<List<LeaderboardEntry>> =
        leaderboardDao.getLeaderboard()
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

    override suspend fun getUserStats(): Result<Unit> =
        withContext(Dispatchers.IO) {
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

    override suspend fun getLeaderboard(): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val oldRank = rankDataStore.currentRank.firstOrNull()
                val response = api.getLeaderboard()
                if (response.isSuccessful) {
                    val responseDto = response.body()
                    val entriesDto = responseDto?.leaderboard ?: emptyList()

                    val newRank = responseDto?.myRank
                    if (newRank != null) {
                        rankDataStore.saveMyRank(newRank)
                        if (oldRank != null && oldRank > 0 && newRank != oldRank) {
                            NotificationUtils.showRankNotification(context, newRank, oldRank)
                        }
                    }

                    val entities =
                        entriesDto.map {
                            LeaderboardEntity(
                                userId = it.userId,
                                nome = it.nome,
                                cognome = it.cognome ?: "",
                                xpTotali = it.xpTotali,
                            )
                        }
                    leaderboardDao.refreshLeaderboard(entities)
                    Result.success(Unit)
                } else {
                    Result.failure(HttpException(response))
                }
            } catch (e: IOException) {
                Result.failure(Exception("Errore di rete: impossibile scaricare la classifica", e))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun runLeaderboardWorkerNow(): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val request = OneTimeWorkRequestBuilder<LeaderboardCheckWorker>().build()
                WorkManager.getInstance(context).enqueue(request)
                Unit
            }
        }
}
