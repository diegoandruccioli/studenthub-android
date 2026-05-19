package com.unibo.android.domain.model

data class UserStats(
    val userId: Int,
    val nome: String,
    val cognome: String,
    val xp: Int,
    val level: Int,
    val rank: Int
)
