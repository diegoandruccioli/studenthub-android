package com.unibo.android.domain.usecase

import com.unibo.android.domain.repository.EsameRepository

class RefreshEsamiUseCase(private val repository: EsameRepository) {
    suspend operator fun invoke() = repository.refreshEsami()
}
