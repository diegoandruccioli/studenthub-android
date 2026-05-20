package com.unibo.android.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LeaderboardEntryDto(
    @SerializedName("id") val userId: Int,
    val nome: String,
    val cognome: String,
    val xpTotali: Int
)

data class GamificationStatusDto(
    val currentXp: Int,
    val currentRank: Int,
    val currentLevel: Int,
    val levelTitle: String,
    val progressPercentage: Int
)
