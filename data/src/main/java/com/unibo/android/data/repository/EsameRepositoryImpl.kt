package com.unibo.android.data.repository

import android.content.Context
import com.unibo.android.data.local.StudentHubDatabase
import com.unibo.android.data.local.entity.EsameEntity
import com.unibo.android.data.local.mapper.toDomain
import com.unibo.android.data.local.mapper.toEntity
import com.unibo.android.data.remote.NetworkClient
import com.unibo.android.data.remote.dto.ExamRequest
import com.unibo.android.domain.model.Esame
import com.unibo.android.domain.repository.EsameRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class EsameRepositoryImpl(context: Context) : EsameRepository {

    private val dao = StudentHubDatabase.getInstance(context).esameDao()
    private val api = NetworkClient.examApiService

    override fun getEsami(): Flow<List<Esame>> =
        dao.getAllEsami()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(Dispatchers.IO)

    override suspend fun addEsame(esame: Esame) = withContext(Dispatchers.IO) {
        val localId = dao.insertEsame(esame.toEntity()).toInt()
        runCatching {
            val response = api.addEsami(
                listOf(ExamRequest(
                    nome = esame.nome,
                    voto = esame.voto,
                    cfu = esame.cfu,
                    lode = esame.lode,
                    data = toApiDate(esame.dataEsame)
                ))
            )
            if (response.isSuccessful) {
                response.body()?.ids?.firstOrNull()?.let { remoteId ->
                    dao.markSynced(localId, remoteId)
                }
            }
        }
        Unit
    }

    override suspend fun updateEsame(esame: Esame) =
        withContext(Dispatchers.IO) { dao.updateEsame(esame.toEntity()) }

    override suspend fun deleteEsame(esame: Esame) = withContext(Dispatchers.IO) {
        val entity = dao.getById(esame.id)
        if (entity?.remoteId != null) {
            runCatching { api.deleteEsame(entity.remoteId) }
        }
        dao.deleteEsame(esame.toEntity())
    }

    override suspend fun refreshEsami() = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getEsami()
            if (response.isSuccessful) {
                response.body()?.forEach { dto ->
                    if (dao.getByRemoteId(dto.id) == null) {
                        dao.insertEsame(
                            EsameEntity(
                                nome = dto.nome,
                                voto = dto.voto,
                                lode = dto.lode,
                                cfu = dto.cfu,
                                dataEsame = dto.data,
                                remoteId = dto.id,
                                pendingSync = false
                            )
                        )
                    }
                }
            }
        }
        Unit
    }

    private fun toApiDate(date: String): String {
        val parts = date.trim().split("/")
        return if (parts.size == 3)
            "${parts[2]}-${parts[1].padStart(2, '0')}-${parts[0].padStart(2, '0')}"
        else date
    }
}
