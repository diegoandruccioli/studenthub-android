package com.unibo.android.domain.usecase

import com.unibo.android.domain.model.PuntoAndamento
import com.unibo.android.domain.model.Statistiche
import com.unibo.android.domain.repository.EsameRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class GetStatisticheUseCase(private val repository: EsameRepository) {
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    operator fun invoke(): Flow<Statistiche> = repository.getEsami().map { esami ->
        if (esami.isEmpty()) {
            return@map Statistiche(0.0, 0, 0.0, emptyList())
        }

        val esamiOrdinati = esami.mapNotNull { esame ->
            try {
                esame to LocalDate.parse(esame.dataEsame, dateFormatter)
            } catch (e: DateTimeParseException) {
                null // Skip invalid dates instead of silent failure with 0L
            }
        }.sortedBy { it.second }

        var sommaProdotti = 0.0
        var sommaCfu = 0
        val andamento = mutableListOf<PuntoAndamento>()

        esamiOrdinati.forEach { (esame, _) ->
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
