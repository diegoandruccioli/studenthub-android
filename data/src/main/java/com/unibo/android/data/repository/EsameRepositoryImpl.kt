package com.unibo.android.data.repository

import android.content.Context
import com.unibo.android.data.local.StudentHubDatabase
import com.unibo.android.data.local.entity.EsameEntity
import com.unibo.android.data.local.mapper.toDomain
import com.unibo.android.data.local.mapper.toEntity
import com.unibo.android.data.remote.NetworkClient
import com.unibo.android.data.remote.dto.ExamRequest
import com.unibo.android.data.remote.dto.StatsResponseDto
import com.unibo.android.domain.model.Esame
import com.unibo.android.domain.model.PuntoAndamento
import com.unibo.android.domain.model.Statistiche
import com.unibo.android.domain.repository.EsameRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
                    data = esame.dataEsame.format(DateTimeFormatter.ISO_LOCAL_DATE)
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

    override suspend fun updateEsame(esame: Esame) = withContext(Dispatchers.IO) {
        val existing = dao.getById(esame.id)
        // pendingSync = true sempre: il SyncWorker lo riprende se l'API call fallisce
        dao.updateEsame(
            esame.toEntity().copy(
                remoteId = existing?.remoteId,
                pendingSync = true
            )
        )
        if (existing?.remoteId != null) {
            runCatching {
                val response = api.updateEsame(
                    existing.remoteId,
                    ExamRequest(
                        nome = esame.nome,
                        voto = esame.voto,
                        cfu = esame.cfu,
                        lode = esame.lode,
                        data = esame.dataEsame.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    )
                )
                if (response.isSuccessful) dao.markSynced(esame.id, existing.remoteId)
            }
        }
        Unit
    }

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
                                dataEsame = LocalDate.parse(dto.data),
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

    override suspend fun getStatisticheRemote(): Result<Statistiche> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getStatistiche()
            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!

                val andamento = dto.chartData.data.mapIndexed { index, voto ->
                    PuntoAndamento(
                        data = LocalDate.now(), // Mapping semplificato come da istruzioni
                        voto = voto,
                        mediaPonderataProgressiva = 0.0
                    )
                }

                Statistiche(
                    mediaPonderata = dto.mediaPonderata,
                    cfuSostenuti = dto.totaleCfu,
                    baseLaurea = dto.baseLaurea,
                    andamentoCarriera = andamento
                )
            } else {
                throw Exception("Errore nel recupero delle statistiche")
            }
        }
    }
}
