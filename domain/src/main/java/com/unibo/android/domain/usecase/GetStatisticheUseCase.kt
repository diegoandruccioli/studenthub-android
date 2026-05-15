package com.unibo.android.domain.usecase

import com.unibo.android.domain.model.PuntoAndamento
import com.unibo.android.domain.model.Statistiche
import com.unibo.android.domain.repository.EsameRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class GetStatisticheUseCase(
    private val repository: EsameRepository,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    operator fun invoke(): Flow<Result<Statistiche>> = repository.getEsami().map { esami ->
        try {
            if (esami.isEmpty()) {
                return@map Result.success(Statistiche(0.0, 0, 0.0, emptyList()))
            }

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

            val mediaPonderataFinal = if (sommaCfu > 0) sommaProdotti / sommaCfu else 0.0
            val baseLaurea = (mediaPonderataFinal * 110.0) / 30.0

            Result.success(
                Statistiche(
                    mediaPonderata = mediaPonderataFinal,
                    cfuSostenuti = sommaCfu,
                    baseLaurea = baseLaurea,
                    andamentoCarriera = andamento
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }.flowOn(defaultDispatcher) // Garantisce la main-safety per calcoli CPU-bound
}
