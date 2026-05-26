package com.unibo.android.data.repository

import android.util.Log
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

private const val TAG = "ObiettivoRepository"

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
        Log.d(TAG, "refreshObiettivi → GET /gamification/badges + my-badges...")
        runCatching {
            // GET /gamification/badges → catalogo completo (nessun dato utente)
            val allBadgesResponse = api.getBadges()
            if (!allBadgesResponse.isSuccessful) {
                throw Exception("getBadges HTTP ${allBadgesResponse.code()}")
            }
            val allBadges = allBadgesResponse.body() ?: emptyList()
            Log.d(TAG, "refreshObiettivi → ${allBadges.size} badge dal catalogo")

            // GET /gamification/my-badges → solo badge sbloccati dall'utente
            val myBadgesResponse = api.getMyBadges()
            val completedIds: Set<Int> = if (myBadgesResponse.isSuccessful) {
                myBadgesResponse.body()?.map { it.safeId }?.toSet() ?: emptySet()
            } else {
                Log.w(TAG, "refreshObiettivi → my-badges HTTP ${myBadgesResponse.code()}, completati=0")
                emptySet()
            }
            Log.d(TAG, "refreshObiettivi → ${completedIds.size} badge completati dall'utente")

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
                Log.d(TAG, "refreshObiettivi → ${entities.size} obiettivi salvati in Room")
            }
        }.onFailure { e ->
            Log.e(TAG, "refreshObiettivi → fallito (Room invariata): ${e.message}", e)
        }
    }
}
