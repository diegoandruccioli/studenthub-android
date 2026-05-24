package com.unibo.android.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LeaderboardEntryDto(
    @SerializedName("id") val userId: Int,
    val nome: String,
    val cognome: String? = "",
    @SerializedName("xp_totali") val xpTotali: Int
)

data class LeaderboardResponseDto(
    val leaderboard: List<LeaderboardEntryDto>,
    val myRank: Int
)

data class GamificationStatusDto(
    @SerializedName("xp_totali") val xpTotali: Int,
    @SerializedName("livello") val livello: LivelloDto,
    @SerializedName("progress") val progress: ProgressDto
)

data class LivelloDto(
    @SerializedName("numero") val numero: Int,
    @SerializedName("nome") val nome: String
)

data class ProgressDto(
    @SerializedName("percentuale") val percentuale: Int,
    @SerializedName("xp_mancanti") val xpMancanti: Int,
    @SerializedName("prossima_soglia") val prossimaSoglia: Int?
)

data class BadgeDto(
    @SerializedName("id") val id: Int,
    @SerializedName("nome") val nome: String,
    @SerializedName("descrizione") val descrizione: String,
    @SerializedName("completato") val completato: Int, // Il backend spesso usa 0/1 per i booleani
    @SerializedName("premio_xp") val premioXp: Int
)
