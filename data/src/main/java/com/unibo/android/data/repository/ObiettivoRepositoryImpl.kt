package com.unibo.android.data.repository

import com.unibo.android.data.local.dao.ObiettivoDao
import com.unibo.android.data.local.mapper.toDomain
import com.unibo.android.domain.model.Obiettivo
import com.unibo.android.domain.repository.ObiettivoRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class ObiettivoRepositoryImpl(
    private val obiettivoDao: ObiettivoDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ObiettivoRepository {

    override fun getObiettivi(): Flow<List<Obiettivo>> =
        obiettivoDao.getAllObiettivi()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(ioDispatcher)

    override suspend fun updateGoalCompletion(id: Int, completed: Boolean) {
        // Operazione atomica diretta nel DB (30L: elimina inefficienza Read-then-Write)
        obiettivoDao.updateGoalCompletionStatus(id, completed)
    }
}
