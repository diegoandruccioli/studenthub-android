package com.unibo.android.ui.screens.statistiche

/**
 * Modello di visualizzazione (UI Model) per la schermata delle statistiche.
 * Rappresenta i dati già formattati e normalizzati per la UI.
 * Seguendo il principio della "Dumb View", la UI non calcola nulla, mostra solo stringhe
 * e disegna punti su coordinate relative (0.0 - 1.0).
 */
data class StatisticheUiModel(
    val mediaPonderata: String,
    val cfuSostenuti: String,
    val baseLaurea: String,
    val puntiVoti: List<OffsetRelativo>,
    val puntiMedia: List<OffsetRelativo>,
    val yMediaFissa: Float // Nuova coordinata Y (0.0 - 1.0) per la linea orizzontale
)

/**
 * Rappresenta una coordinata normalizzata (x e y tra 0.0 e 1.0) per il grafico.
 */
data class OffsetRelativo(
    val x: Float,
    val y: Float
)
