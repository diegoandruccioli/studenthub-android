package com.unibo.android.data.remote

import com.google.gson.annotations.SerializedName

data class LeaderboardResponseDto(
    @SerializedName("rank") val rank: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("nome") val nome: String,
    @SerializedName("cognome") val cognome: String,
    @SerializedName("xp") val xp: Int
)
