package com.unibo.android.domain.model

data class LeaderboardEntry(
    val userId: Int,
    val nome: String,
    val cognome: String,
    val xpTotali: Int,
    val isMe: Boolean = false
)
