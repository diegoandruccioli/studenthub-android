package com.unibo.android.domain.model

data class UserStats(
    val userId: Int,
    val xp: Int,
    val rank: Int,
    val level: Int,
    val levelTitle: String,
    val progressPercentage: Float,
    val xpLabel: String
)

data class LeaderboardEntry(
    val userId: Int,
    val nome: String,
    val cognome: String,
    val xpTotali: Int,
    val isMe: Boolean = false
)
