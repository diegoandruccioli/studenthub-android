package com.unibo.android.data.remote.dto

data class ExamDto(
    val id: Int,
    val nome: String,
    val voto: Int,
    val cfu: Int,
    val lode: Boolean,
    val data: String
)

data class ExamRequest(
    val nome: String,
    val voto: Int,
    val cfu: Int,
    val lode: Boolean,
    val data: String
)

data class AddExamResponse(
    val ids: List<Int>,
    val totalXp: Int?,
    val newBadges: List<Any>?
)

data class UpdateExamResponse(
    val newBadges: List<Any>?,
    val revokedBadgeIds: List<Any>?,
    val xpDifference: Int?
)
