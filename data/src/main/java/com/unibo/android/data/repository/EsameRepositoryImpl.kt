package com.unibo.android.data.repository

import android.content.Context
import com.unibo.android.data.local.StudentHubDatabase
import com.unibo.android.data.local.mapper.toDomain
import com.unibo.android.data.local.mapper.toEntity
import com.unibo.android.domain.model.Esame
import com.unibo.android.domain.repository.EsameRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class EsameRepositoryImpl(context: Context) : EsameRepository {

    private val dao = StudentHubDatabase.getInstance(context).esameDao()

    override fun getEsami(): Flow<List<Esame>> =
        dao.getAllEsami()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(Dispatchers.IO)

    override suspend fun addEsame(esame: Esame) =
        withContext(Dispatchers.IO) { dao.insertEsame(esame.toEntity()) }

    override suspend fun updateEsame(esame: Esame) =
        withContext(Dispatchers.IO) { dao.updateEsame(esame.toEntity()) }

    override suspend fun deleteEsame(esame: Esame) =
        withContext(Dispatchers.IO) { dao.deleteEsame(esame.toEntity()) }
}
