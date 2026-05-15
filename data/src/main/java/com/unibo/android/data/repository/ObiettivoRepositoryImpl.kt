package com.unibo.android.data.repository

import android.content.Context
import com.unibo.android.data.local.StudentHubDatabase
import com.unibo.android.data.local.mapper.toDomain
import com.unibo.android.domain.model.Obiettivo
import com.unibo.android.domain.repository.ObiettivoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ObiettivoRepositoryImpl(context: Context) : ObiettivoRepository {

    private val db = StudentHubDatabase.getInstance(context)
    private val obiettivoDao = db.obiettiviDao()

    override fun getObiettivi(): Flow<List<Obiettivo>> =
        obiettivoDao.getAllObiettivi()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(Dispatchers.IO)

    override suspend fun updateGoalCompletion(id: Int, completed: Boolean) {
        withContext(Dispatchers.IO) {
            val current = obiettivoDao.getObiettivoById(id)
            if (current != null && current.completato != completed) {
                obiettivoDao.updateObiettivo(current.copy(completato = completed))
            }
        }
    }
}
