package com.unibo.android.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.unibo.android.data.local.StudentHubDatabase
import com.unibo.android.data.remote.NetworkClient
import com.unibo.android.data.remote.dto.ExamRequest
import java.time.format.DateTimeFormatter

class SyncExamsWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val dao = StudentHubDatabase.getInstance(applicationContext).esameDao()
        val api = NetworkClient.examApiService
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        val pending = dao.getUnsyncedEsami()

        if (pending.isEmpty()) return Result.success()

        var hasError = false

        pending.forEach { entity ->
            val request = ExamRequest(
                nome = entity.nome,
                voto = entity.voto,
                cfu = entity.cfu,
                lode = entity.lode,
                data = entity.dataEsame.format(formatter)
            )

            val synced = if (entity.remoteId != null) {
                runCatching {
                    val response = api.updateEsame(entity.remoteId, request)
                    when {
                        response.isSuccessful -> {
                            dao.markSynced(entity.id, entity.remoteId)
                            true
                        }
                        response.code() == 404 -> {
                            // Esame rimosso dal server: resetta remoteId per ricrearlo al prossimo run
                            dao.updateEsame(entity.copy(remoteId = null))
                            true
                        }
                        else -> false
                    }
                }.getOrDefault(false)
            } else {
                runCatching {
                    val response = api.addEsami(listOf(request))
                    if (response.isSuccessful) {
                        response.body()?.ids?.firstOrNull()?.let { remoteId ->
                            dao.markSynced(entity.id, remoteId)
                        }
                        true
                    } else false
                }.getOrDefault(false)
            }

            if (!synced) hasError = true
        }

        return if (hasError) Result.retry() else Result.success()
    }
}
