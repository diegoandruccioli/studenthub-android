package com.unibo.android.data.repository

import android.content.Context
import com.unibo.android.data.local.StudentHubDatabase
import com.unibo.android.data.local.entity.EsameEntity
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ObiettivoRepositoryImpl(context: Context) : ObiettivoRepository {

    private val db = StudentHubDatabase.getInstance(context)
    private val obiettivoDao = db.obiettiviDao()
    private val esameDao = db.esameDao()

    override fun getObiettivi(): Flow<List<Obiettivo>> =
        obiettivoDao.getAllObiettivi()
            .map { list -> 
                if (list.isEmpty()) {
                    prepopulateSync()
                    // The flow will emit again once data is inserted
                }
                list.map { it.toDomain() } 
            }
            .flowOn(Dispatchers.IO)

    private fun prepopulateSync() {
        // We can't use suspend here easily inside map, but we can launch a coroutine
        CoroutineScope(Dispatchers.IO).launch {
            val current = db.obiettiviDao().getAllObiettiviSync() // Need to add this to DAO
            if (current.isEmpty()) {
                db.obiettiviDao().insertObiettivi(
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

    override suspend fun checkAndUpdateObiettivi() {
        withContext(Dispatchers.IO) {
            val esami = esameDao.getAllEsamiSync()
            
            // 1. Primo Passo: almeno 1 esame
            if (esami.isNotEmpty()) {
                updateGoalCompletion(1, true)
            } else {
                updateGoalCompletion(1, false)
            }

            // 2. Secchione: almeno 1 lode
            if (esami.any { it.lode }) {
                updateGoalCompletion(2, true)
            } else {
                updateGoalCompletion(2, false)
            }

            // 3. Maratoneta: 3 esami in un mese
            if (checkMaratoneta(esami)) {
                updateGoalCompletion(3, true)
            } else {
                updateGoalCompletion(3, false)
            }

            // 4. Giro di Boa: 90 CFU
            if (esami.sumOf { it.cfu } >= 90) {
                updateGoalCompletion(4, true)
            } else {
                updateGoalCompletion(4, false)
            }
        }
    }

    private suspend fun updateGoalCompletion(id: Int, completed: Boolean) {
        val current = obiettivoDao.getObiettivoById(id)
        if (current != null && current.completato != completed) {
            obiettivoDao.updateObiettivo(current.copy(completato = completed))
        }
    }

    private fun checkMaratoneta(esami: List<EsameEntity>): Boolean {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ITALY)
        val monthYearCounts = mutableMapOf<String, Int>()
        
        esami.forEach { esame ->
            try {
                val date = sdf.parse(esame.dataEsame)
                if (date != null) {
                    val cal = Calendar.getInstance()
                    cal.time = date
                    val key = "${cal.get(Calendar.MONTH)}-${cal.get(Calendar.YEAR)}"
                    monthYearCounts[key] = (monthYearCounts[key] ?: 0) + 1
                }
            } catch (e: Exception) {
                // Ignore parse errors
            }
        }
        
        return monthYearCounts.values.any { it >= 3 }
    }
}
