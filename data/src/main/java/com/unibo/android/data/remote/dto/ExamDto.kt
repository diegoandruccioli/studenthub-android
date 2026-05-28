package com.unibo.android.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ExamDto(
    val id: Int,
    val nome: String,
    val voto: Int,
    val cfu: Int,
    // MySQL TINYINT(1) serializzato da mysql2 come 0/1, non come boolean
    val lode: Int,
    val data: String,
)

data class ExamRequest(
    val nome: String,
    val voto: Int,
    val cfu: Int,
    val lode: Boolean,
    val data: String,
)

data class AddExamResponse(
    val ids: List<Int>,
    @SerializedName("xp_totali_guadagnati") val totalXp: Int?,
    @SerializedName("nuovi_badge") val newBadges: List<Any>?,
)

data class UpdateExamResponse(
    @SerializedName("nuovi_badge") val newBadges: List<Any>?,
    @SerializedName("badge_revocati") val revokedBadgeIds: List<Any>?,
    @SerializedName("xp_difference") val xpDifference: Int?,
)
