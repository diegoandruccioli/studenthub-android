package com.unibo.android.domain.usecase

import com.unibo.android.domain.model.Esame
import com.unibo.android.domain.model.PuntoAndamento
import com.unibo.android.domain.model.Statistiche

/**
 * Oggetto dedicato esclusivamente a eseguire i calcoli matematici offline per le statistiche.
 * Isola la logica di business per favorire la testabilità e la manutenibilità.
 */
object StatisticheCalculator {
    fun calcola(esami: List<Esame>): Statistiche {
        if (esami.isEmpty()) return Statistiche(0.0, 0, 0.0, emptyList())

        val esamiOrdinati = esami.sortedBy { it.dataEsame }
        var sommaProdotti = 0.0
        var sommaCfu = 0
        val andamento = mutableListOf<PuntoAndamento>()

        esamiOrdinati.forEach { esame ->
            sommaCfu += esame.cfu
            sommaProdotti += esame.voto * esame.cfu
            val mediaCorrente = if (sommaCfu > 0) sommaProdotti / sommaCfu else 0.0
            
            andamento.add(
                PuntoAndamento(
                    data = esame.dataEsame,
                    voto = esame.voto,
                    mediaPonderataProgressiva = mediaCorrente
                )
            )
        }

        val mediaFinal = if (sommaCfu > 0) sommaProdotti / sommaCfu else 0.0
        return Statistiche(
            mediaPonderata = mediaFinal,
            cfuSostenuti = sommaCfu,
            baseLaurea = (mediaFinal * 110.0) / 30.0,
            andamentoCarriera = andamento
        )
    }
}
