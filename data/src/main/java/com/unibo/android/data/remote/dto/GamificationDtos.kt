package com.unibo.android.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GamificationStatusDto(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("xp") val xp: Int,
    @SerializedName("rank") val rank: Int,
    @SerializedName("level") val level: Int? = null // Alcuni backend potrebbero ritornarlo
)

data class LeaderboardEntryDto(
    @SerializedName("user_id") val id: Int,
    @SerializedName("nome") val nome: String,
    @SerializedName("xp_totali") val xpTotali: Int
)

data class LeaderboardResponseDto(
    @SerializedName("leaderboard") val leaderboard: List<LeaderboardEntryDto>,
    @SerializedName("myRank") val myRank: Int
)
