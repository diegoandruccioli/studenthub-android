package com.unibo.android.domain.model

data class LeaderboardEntry(
    val rank: Int,
    val userId: Int,
    val nome: String,
    val cognome: String,
    val xp: Int,
    val isCurrentUser: Boolean
)
