package com.unibo.android.domain.usecase

import com.unibo.android.domain.model.Obiettivo
import com.unibo.android.domain.repository.ObiettivoRepository
import kotlinx.coroutines.flow.Flow

class GetObiettiviUseCase(private val repository: ObiettivoRepository) {
    operator fun invoke(): Flow<List<Obiettivo>> = repository.getObiettivi()
}
