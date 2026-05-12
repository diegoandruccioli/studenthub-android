package com.unibo.android.domain.model

data class Statistiche(
    val mediaPonderata: Double,
    val cfuSostenuti: Int,
    val baseLaurea: Double,
    val andamentoCarriera: List<PuntoAndamento>
)

data class PuntoAndamento(
    val data: String,
    val voto: Int,
    val mediaPonderataProgressiva: Double
)
