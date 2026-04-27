package com.unibo.android.domain.model

data class Esame(
    val id: Int = 0,
    val nome: String,
    val voto: Int,
    val lode: Boolean,
    val cfu: Int,
    val dataEsame: String
)
