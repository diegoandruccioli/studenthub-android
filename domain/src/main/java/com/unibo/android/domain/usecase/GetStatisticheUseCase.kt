package com.unibo.android.domain.usecase

import com.unibo.android.domain.model.Statistiche
import com.unibo.android.domain.repository.EsameRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

class GetStatisticheUseCase(
    private val repository: EsameRepository
) {
    operator fun invoke(): Flow<Result<Statistiche>> = flow {
        try {
            // TENTATIVO 1: ONLINE (Backend remoto)
            val remoteStats = repository.getStatisticheRemote()
            
            remoteStats.onSuccess { stats ->
                emit(Result.success(stats))
            }.onFailure { 
                throw it // Fallback al blocco catch
            }
        } catch (e: Exception) {
            // TENTATIVO 2: OFFLINE (Fallback locale)
            try {
                val esamiLocali = repository.getEsami().first()
                val statsLocali = StatisticheCalculator.calcola(esamiLocali)
                emit(Result.success(statsLocali))
            } catch (localError: Exception) {
                emit(Result.failure(localError))
            }
        }
    }
}
