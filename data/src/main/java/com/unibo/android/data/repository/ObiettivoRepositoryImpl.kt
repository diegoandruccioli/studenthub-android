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
            val response = api.getBadges()
            if (response.isSuccessful) {
                val badges = response.body() ?: emptyList()
                val entities = badges.map { dto ->
                    ObiettivoEntity(
                        id = dto.id,
                        nome = dto.nome,
                        descrizione = dto.descrizione,
                        completato = dto.completato == 1,
                        premioXp = dto.premioXp
                    )
                }
                obiettivoDao.insertAll(entities)
                Unit
            } else {
                throw Exception("Errore nel caricamento degli obiettivi")
            }
        }
    }
}
