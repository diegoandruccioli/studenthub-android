package com.unibo.android.domain.usecase

import com.unibo.android.domain.repository.ObiettivoRepository

class CheckObiettiviUseCase(private val repository: ObiettivoRepository) {
    suspend operator fun invoke() = repository.checkAndUpdateObiettivi()
}
