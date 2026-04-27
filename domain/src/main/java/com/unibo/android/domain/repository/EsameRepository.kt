package com.unibo.android.domain.repository

import com.unibo.android.domain.model.Esame
import kotlinx.coroutines.flow.Flow

interface EsameRepository {
    fun getEsami(): Flow<List<Esame>>
    suspend fun addEsame(esame: Esame)
    suspend fun updateEsame(esame: Esame)
    suspend fun deleteEsame(esame: Esame)
}
