package com.unibo.android.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing an entry in the global leaderboard.
 */
@Entity(tableName = "leaderboard")
data class LeaderboardEntity(
    @PrimaryKey val userId: Int,
    val nome: String,
    val cognome: String,
    val xpTotali: Int
)
