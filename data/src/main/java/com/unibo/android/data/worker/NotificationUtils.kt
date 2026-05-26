package com.unibo.android.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

object NotificationUtils {
    fun showRankNotification(context: Context, newRank: Int, oldRank: Int) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "gamification_rank_updates"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, 
                "Aggiornamenti Classifica", 
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifiche relative ai cambiamenti di posizione in classifica"
                enableLights(true)
                enableVibration(true)
            }
            manager.createNotificationChannel(channel)
        }
        
        val title = if (newRank < oldRank) "Rank migliorato!" else "Rank cambiato"
        val message = if (newRank < oldRank) {
            "Sei salito in classifica! Ora sei al posto #$newRank 🎉"
        } else {
            "Sei sceso in classifica. Ora sei al posto #$newRank"
        }
        
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .build()
            
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
