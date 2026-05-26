package com.unibo.android.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.unibo.android.data.R
import com.unibo.android.data.local.RankDataStore
import com.unibo.android.data.repository.GamificationRepositoryImpl
import kotlinx.coroutines.flow.firstOrNull

class LeaderboardCheckWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    private val rankDataStore = RankDataStore(appContext)
    private val repository = GamificationRepositoryImpl(appContext)

    override suspend fun doWork(): Result {
        return try {
            val oldRank = rankDataStore.currentRank.firstOrNull()

            // getLeaderboard() aggiorna currentRank in RankDataStore via saveMyRank();
            // getUserStats() aggiorna solo xp/level/progress, NON il rank.
            val leaderboardResult = repository.getLeaderboard()
            if (leaderboardResult.isSuccess) {
                val newRank = rankDataStore.currentRank.firstOrNull()

                if (oldRank != null && newRank != null && newRank != oldRank) {
                    if (newRank < oldRank) {
                        showNotification(
                            "Rank migliorato!",
                            "Sei salito in classifica! Ora sei al posto #$newRank 🎉"
                        )
                    } else {
                        showNotification(
                            "Rank cambiato",
                            "Sei sceso in classifica. Ora sei al posto #$newRank"
                        )
                    }
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun showNotification(title: String, message: String) {
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "gamification_channel"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Gamification", NotificationManager.IMPORTANCE_DEFAULT)
            manager.createNotificationChannel(channel)
        }
        
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            // L'icona andrebbe impostata a una esistente, usiamo un placeholder standard o mipmap/ic_launcher
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
            
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
