package com.unibo.android.domain.usecase

import com.unibo.android.domain.model.Esame
import com.unibo.android.domain.repository.EsameRepository
import kotlinx.coroutines.flow.Flow

class GetEsamiUseCase(private val repository: EsameRepository) {
    operator fun invoke(): Flow<List<Esame>> = repository.getEsami()
}
