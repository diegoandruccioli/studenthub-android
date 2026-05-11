package com.unibo.android.domain.repository

import com.unibo.android.domain.model.Obiettivo
import kotlinx.coroutines.flow.Flow

interface ObiettivoRepository {
    fun getObiettivi(): Flow<List<Obiettivo>>
    suspend fun checkAndUpdateObiettivi()
}
