package com.unibo.android.domain.usecase

import com.unibo.android.domain.model.Esame
import com.unibo.android.domain.repository.EsameRepository

class DeleteEsameUseCase(private val repository: EsameRepository) {
    suspend operator fun invoke(esame: Esame) = repository.deleteEsame(esame)
}
