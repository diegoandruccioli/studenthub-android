package com.unibo.android.domain.model

data class Obiettivo(
    val id: Int,
    val nome: String,
    val descrizione: String,
    val completato: Boolean,
    val premioXp: Int
)
