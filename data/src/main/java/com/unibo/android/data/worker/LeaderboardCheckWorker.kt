package com.unibo.android.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.unibo.android.data.local.RankDataStore
import com.unibo.android.data.repository.GamificationRepositoryImpl

class LeaderboardCheckWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {
    private val rankDataStore = RankDataStore(appContext)
    private val repository = GamificationRepositoryImpl(appContext)

    override suspend fun doWork(): Result {
        return try {
            rankDataStore.saveLastCheckTimestamp(System.currentTimeMillis())
            val leaderboardResult = repository.getLeaderboard()
            if (leaderboardResult.isSuccess) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
