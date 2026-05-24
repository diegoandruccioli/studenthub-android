package com.unibo.android.domain.usecase

import com.unibo.android.domain.repository.ObiettivoRepository

class RefreshObiettiviUseCase(private val repository: ObiettivoRepository) {
    suspend operator fun invoke() = repository.refreshObiettivi()
}
