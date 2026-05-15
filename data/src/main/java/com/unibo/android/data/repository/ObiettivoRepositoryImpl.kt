package com.unibo.android.data.repository

import android.content.Context
import com.unibo.android.data.local.StudentHubDatabase
import com.unibo.android.data.local.mapper.toDomain
import com.unibo.android.domain.model.Obiettivo
import com.unibo.android.domain.repository.ObiettivoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ObiettivoRepositoryImpl(context: Context) : ObiettivoRepository {

    private val db = StudentHubDatabase.getInstance(context)
    private val obiettivoDao = db.obiettiviDao()

    override fun getObiettivi(): Flow<List<Obiettivo>> =
        obiettivoDao.getAllObiettivi()
            .map { list -> 
                if (list.isEmpty()) {
                    prepopulateSync()
                }
                list.map { it.toDomain() } 
            }
            .flowOn(Dispatchers.IO)

    private fun prepopulateSync() {
        CoroutineScope(Dispatchers.IO).launch {
            val current = obiettivoDao.getAllObiettiviSync()
            if (current.isEmpty()) {
                obiettivoDao.insertObiettivi(
                    listOf(
                        com.unibo.android.data.local.entity.ObiettivoEntity(1, "Primo Passo", "Registra il tuo primo esame superato", false, 150),
                        com.unibo.android.data.local.entity.ObiettivoEntity(2, "Secchione", "Ottieni la tua prima Lode", false, 300),
                        com.unibo.android.data.local.entity.ObiettivoEntity(3, "Maratoneta", "Supera 3 esami in un mese", false, 500),
                        com.unibo.android.data.local.entity.ObiettivoEntity(4, "Giro di Boa", "Raggiungi 90 CFU", false, 800)
                    )
                )
            }
        }
    }

    override suspend fun updateGoalCompletion(id: Int, completed: Boolean) {
        withContext(Dispatchers.IO) {
            val current = obiettivoDao.getObiettivoById(id)
            if (current != null && current.completato != completed) {
                obiettivoDao.updateObiettivo(current.copy(completato = completed))
            }
        }
    }
}
