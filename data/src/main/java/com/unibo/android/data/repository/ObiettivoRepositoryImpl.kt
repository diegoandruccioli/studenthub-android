package com.unibo.android.data.repository

import com.unibo.android.data.local.dao.ObiettivoDao
import com.unibo.android.data.local.entity.ObiettivoEntity
import com.unibo.android.data.local.mapper.toDomain
import com.unibo.android.data.remote.NetworkClient
import com.unibo.android.domain.model.Obiettivo
import com.unibo.android.domain.repository.ObiettivoRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ObiettivoRepositoryImpl(
    private val obiettivoDao: ObiettivoDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ObiettivoRepository {

    private val api = NetworkClient.gamificationApiService

    override fun getObiettivi(): Flow<List<Obiettivo>> =
        obiettivoDao.getAllObiettivi()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(ioDispatcher)

    override suspend fun refreshObiettivi(): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            // GET /gamification/badges → catalogo completo (nessun dato utente)
            val allBadgesResponse = api.getBadges()
            if (!allBadgesResponse.isSuccessful) throw Exception("Errore nel caricamento degli obiettivi")
            val allBadges = allBadgesResponse.body() ?: emptyList()

            // GET /gamification/my-badges → solo badge sbloccati dall'utente
            val myBadgesResponse = api.getMyBadges()
            val completedIds: Set<Int> = if (myBadgesResponse.isSuccessful) {
                myBadgesResponse.body()?.map { it.safeId }?.toSet() ?: emptySet()
            } else {
                emptySet()
            }

            // Merge: catalogo + stato completamento utente
            val entities = allBadges.map { dto ->
                ObiettivoEntity(
                    id = dto.safeId,
                    nome = dto.nome,
                    descrizione = dto.descrizione,
                    completato = dto.safeId in completedIds,
                    premioXp = dto.xpValore
                )
            }

            if (entities.isNotEmpty()) {
                obiettivoDao.insertAll(entities)
            }
        }
    }
}
