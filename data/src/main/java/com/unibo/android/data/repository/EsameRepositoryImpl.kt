package com.unibo.android.data.repository

import android.content.Context
import android.util.Log
import com.unibo.android.data.local.StudentHubDatabase
import com.unibo.android.data.local.entity.EsameEntity
import com.unibo.android.data.local.mapper.toDomain
import com.unibo.android.data.local.mapper.toEntity
import com.unibo.android.data.remote.NetworkClient
import com.unibo.android.data.remote.dto.ExamRequest
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

private const val TAG = "EsameRepository"

class EsameRepositoryImpl(context: Context) : EsameRepository {

    private val dao = StudentHubDatabase.getInstance(context).esameDao()
    private val api = NetworkClient.examApiService

    override fun getEsami(): Flow<List<Esame>> =
        dao.getAllEsami()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(Dispatchers.IO)

    override suspend fun addEsame(esame: Esame): Result<Unit> = withContext(Dispatchers.IO) {
        val localId = dao.insertEsame(esame.toEntity()).toInt()
        Log.d(TAG, "addEsame → salvato in Room localId=$localId, invio al server...")
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
                val remoteId = response.body()?.ids?.firstOrNull()
                Log.d(TAG, "addEsame → server OK, remoteId=$remoteId")
                remoteId?.let { dao.markSynced(localId, it) }
                Unit
            } else {
                throw Exception("addEsame HTTP ${response.code()}")
            }
        }.onFailure { e ->
            Log.e(TAG, "addEsame → sync fallito (pendingSync=true, retry via Worker): ${e.message}", e)
        }
    }

    override suspend fun updateEsame(esame: Esame): Result<Unit> = withContext(Dispatchers.IO) {
        val existing = dao.getById(esame.id)
        dao.updateEsame(
            esame.toEntity().copy(
                remoteId = existing?.remoteId,
                pendingSync = true
            )
        )
        if (existing?.remoteId != null) {
            Log.d(TAG, "updateEsame → id=${esame.id} remoteId=${existing.remoteId}, invio al server...")
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
                if (response.isSuccessful) {
                    Log.d(TAG, "updateEsame → server OK")
                    dao.markSynced(esame.id, existing.remoteId)
                    Unit
                } else {
                    throw Exception("updateEsame HTTP ${response.code()}")
                }
            }.onFailure { e ->
                Log.e(TAG, "updateEsame → sync fallito (pendingSync=true, retry via Worker): ${e.message}", e)
            }
        } else {
            Log.w(TAG, "updateEsame → id=${esame.id} non ha remoteId, skip sync")
            Result.success(Unit)
        }
    }

    override suspend fun deleteEsame(esame: Esame): Result<Unit> = withContext(Dispatchers.IO) {
        val entity = dao.getById(esame.id) ?: return@withContext Result.success(Unit)
        if (entity.remoteId != null) {
            Log.d(TAG, "deleteEsame → id=${esame.id} remoteId=${entity.remoteId}, invio al server...")
            val response = runCatching { api.deleteEsame(entity.remoteId) }
                .onFailure { e -> Log.e(TAG, "deleteEsame → chiamata fallita: ${e.message}", e) }
                .getOrNull()
            if (response?.isSuccessful == true) {
                response.body()?.close()
                Log.d(TAG, "deleteEsame → server OK, rimosso da Room")
                dao.deleteEsame(entity)
            } else {
                Log.w(TAG, "deleteEsame → server risposto ${response?.code()} oppure offline, marcato pendingDelete")
                dao.markPendingDelete(esame.id)
            }
        } else {
            Log.d(TAG, "deleteEsame → id=${esame.id} senza remoteId, solo Room")
            dao.deleteEsame(entity)
        }
        Result.success(Unit)
    }

    override suspend fun refreshEsami() {
        withContext(Dispatchers.IO) {
            Log.d(TAG, "refreshEsami → GET /exams...")
            runCatching {
                val response = api.getEsami()
                if (!response.isSuccessful) {
                    Log.e(TAG, "refreshEsami → server risposto HTTP ${response.code()}")
                    return@runCatching
                }
                Log.d(TAG, "refreshEsami → server OK, ${response.body()?.size ?: 0} esami ricevuti")
                val pendingLocal = dao.getUnsyncedEsami()
                response.body()?.forEach { dto ->
                    val remoteDate = LocalDate.parse(dto.data.take(10))
                    val existing = dao.getByRemoteId(dto.id)
                    when {
                        existing == null -> {
                            // Esame presente sul server ma non in locale: inserimento
                            val lodeBool = dto.lode != 0
                            val pendingMatch = pendingLocal.firstOrNull { p ->
                                p.remoteId == null &&
                                p.nome == dto.nome &&
                                p.voto == dto.voto &&
                                p.dataEsame == remoteDate
                            }
                            if (pendingMatch != null) {
                                dao.markSynced(pendingMatch.id, dto.id)
                            } else {
                                dao.insertEsame(
                                    EsameEntity(
                                        nome = dto.nome,
                                        voto = dto.voto,
                                        lode = lodeBool,
                                        cfu = dto.cfu,
                                        dataEsame = remoteDate,
                                        remoteId = dto.id,
                                        pendingSync = false
                                    )
                                )
                            }
                        }
                        !existing.pendingSync && !existing.pendingDelete -> {
                            // Esame già sincronizzato: aggiorna se il server ha dati più recenti
                            val lodeBool = dto.lode != 0
                            val changed = existing.nome != dto.nome ||
                                          existing.voto != dto.voto ||
                                          existing.lode != lodeBool ||
                                          existing.cfu != dto.cfu ||
                                          existing.dataEsame != remoteDate
                            if (changed) {
                                dao.updateEsame(
                                    existing.copy(
                                        nome = dto.nome,
                                        voto = dto.voto,
                                        lode = lodeBool,
                                        cfu = dto.cfu,
                                        dataEsame = remoteDate
                                    )
                                )
                            }
                        }
                        // existing.pendingSync == true → modifica locale in attesa: locale vince
                        // existing.pendingDelete == true → marcato per eliminazione: ignora
                    }
                }
            }.onFailure { e ->
                Log.e(TAG, "refreshEsami → eccezione (Room invariata): ${e.message}", e)
            }
        }
    }

    override suspend fun getStatisticheRemote(): Result<Statistiche> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getStatistiche()
            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!

                val andamento = dto.chartData.data.mapIndexed { index, voto ->
                    PuntoAndamento(
                        data = LocalDate.now(),
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

    override val totalXpFlow: Flow<Int> = dao.getTotalXp()
        .map { it ?: 0 }
        .flowOn(Dispatchers.IO)
}
