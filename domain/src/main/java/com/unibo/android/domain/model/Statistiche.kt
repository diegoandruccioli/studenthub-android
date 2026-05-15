package com.unibo.android.domain.model

import java.time.LocalDate

data class Statistiche(
    val mediaPonderata: Double,
    val cfuSostenuti: Int,
    val baseLaurea: Double,
    val andamentoCarriera: List<PuntoAndamento>
)

data class PuntoAndamento(
    val data: LocalDate,
    val voto: Int,
    val mediaPonderataProgressiva: Double
)
