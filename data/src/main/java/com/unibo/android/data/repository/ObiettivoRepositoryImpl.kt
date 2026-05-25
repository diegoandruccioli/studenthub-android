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
                val badgesDto = response.body() ?: emptyList()
                
                // Mappatura DTO -> Entity per aggiornare l'intero catalogo
                // Il backend fornisce sia gli sbloccati che i non sbloccati (se configurato)
                // Se fornisce solo gli sbloccati, usiamo il pattern "reset e mark"
                
                val entities = badgesDto.map { dto ->
                    ObiettivoEntity(
                        id = dto.safeId,
                        nome = dto.nome,
                        descrizione = dto.descrizione,
                        completato = dto.completato == 1 || (dto.idObiettivo != null), 
                        premioXp = dto.xpValore
                    )
                }

                if (entities.isNotEmpty()) {
                    // Aggiornamento atomico del catalogo e dello stato
                    obiettivoDao.insertAll(entities)
                }

                Unit
            } else {
                throw Exception("Errore nel caricamento degli obiettivi")
            }
        }
    }
}
