package com.unibo.android.domain.usecase

import com.unibo.android.domain.model.Statistiche
import com.unibo.android.domain.repository.EsameRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class GetStatisticheUseCase(
    private val repository: EsameRepository
) {
    /**
     * Flusso reattivo: si aggiorna automaticamente a ogni modifica degli esami in Room.
     * Il calcolo locale usa StatisticheCalculator che produce mediaPonderataProgressiva
     * corretta per ogni esame — il backend hardcoda questo valore a 0.0, quindi
     * la sorgente locale è preferibile per l'andamento del grafico.
     */
    operator fun invoke(): Flow<Result<Statistiche>> =
        repository.getEsami()
            .map { esami -> Result.success(StatisticheCalculator.calcola(esami)) }
            .catch { e -> emit(Result.failure(e)) }
}
