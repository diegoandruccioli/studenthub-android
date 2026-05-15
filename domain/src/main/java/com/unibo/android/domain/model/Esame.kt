package com.unibo.android.domain.model

import java.time.LocalDate

data class Esame(
    val id: Int = 0,
    val nome: String,
    val voto: Int,
    val lode: Boolean,
    val cfu: Int,
    val dataEsame: LocalDate
)
