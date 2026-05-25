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
        // TENTATIVO 1: ONLINE (Backend remoto)
        val remote = repository.getStatisticheRemote()

        // Compute result outside any catch — required by Flow exception transparency
        // (emit() inside a catch block causes IllegalStateException when the downstream
        //  collector throws AbortFlowException after flow.first() receives its value)
        val result: Result<Statistiche> = if (remote.isSuccess) {
            remote
        } else {
            // TENTATIVO 2: OFFLINE (Fallback locale)
            try {
                val esamiLocali = repository.getEsami().first()
                Result.success(StatisticheCalculator.calcola(esamiLocali))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        emit(result) // always outside any catch block
    }
}
