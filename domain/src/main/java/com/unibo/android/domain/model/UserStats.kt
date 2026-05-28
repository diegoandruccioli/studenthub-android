package com.unibo.android.domain.model

data class UserStats(
    val userId: Int,
    val xp: Int,
    val rank: Int,
    val level: Int,
    val levelTitle: String,
    val progressPercentage: Float,
    val xpLabel: String,
    val prossimaSoglia: Int? = null,
)
