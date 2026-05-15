package com.unibo.android.domain.usecase

import com.unibo.android.domain.model.Obiettivo
import com.unibo.android.domain.repository.ObiettivoRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

class GetObiettiviUseCase(
    private val repository: ObiettivoRepository,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    operator fun invoke(): Flow<List<Obiettivo>> = 
        repository.getObiettivi().flowOn(defaultDispatcher)
}
