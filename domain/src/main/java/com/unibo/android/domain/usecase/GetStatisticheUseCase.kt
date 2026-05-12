package com.unibo.android.domain.usecase

import com.unibo.android.domain.model.PuntoAndamento
import com.unibo.android.domain.model.Statistiche
import com.unibo.android.domain.repository.EsameRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Locale

class GetStatisticheUseCase(private val repository: EsameRepository) {
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ITALY)

    operator fun invoke(): Flow<Statistiche> = repository.getEsami().map { esami ->
        if (esami.isEmpty()) {
            return@map Statistiche(0.0, 0, 0.0, emptyList())
        }

        val esamiOrdinati = esami.sortedBy {
            try {
                dateFormat.parse(it.dataEsame)?.time ?: 0L
            } catch (e: Exception) {
                0L
            }
        }

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

        val mediaPonderataFinal = if (sommaCfu > 0) sommaProdotti / sommaCfu else 0.0
        val baseLaurea = (mediaPonderataFinal * 110.0) / 30.0

        Statistiche(
            mediaPonderata = mediaPonderataFinal,
            cfuSostenuti = sommaCfu,
            baseLaurea = baseLaurea,
            andamentoCarriera = andamento
        )
    }
}
