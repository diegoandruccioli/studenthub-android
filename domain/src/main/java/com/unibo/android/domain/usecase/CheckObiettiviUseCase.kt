package com.unibo.android.domain.usecase

import com.unibo.android.domain.model.Esame
import com.unibo.android.domain.repository.EsameRepository
import com.unibo.android.domain.repository.ObiettivoRepository
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CheckObiettiviUseCase(
    private val esameRepository: EsameRepository,
    private val obiettivoRepository: ObiettivoRepository
) {
    suspend operator fun invoke() {
        val esami = esameRepository.getEsami().first()

        // 1. Primo Passo: almeno 1 esame
        if (esami.isNotEmpty()) {
            obiettivoRepository.updateGoalCompletion(1, true)
        } else {
            obiettivoRepository.updateGoalCompletion(1, false)
        }

        // 2. Secchione: almeno 1 lode
        if (esami.any { it.lode }) {
            obiettivoRepository.updateGoalCompletion(2, true)
        } else {
            obiettivoRepository.updateGoalCompletion(2, false)
        }

        // 3. Maratoneta: 3 esami in un mese
        if (checkMaratoneta(esami)) {
            obiettivoRepository.updateGoalCompletion(3, true)
        } else {
            obiettivoRepository.updateGoalCompletion(3, false)
        }

        // 4. Giro di Boa: 90 CFU
        if (esami.sumOf { it.cfu } >= 90) {
            obiettivoRepository.updateGoalCompletion(4, true)
        } else {
            obiettivoRepository.updateGoalCompletion(4, false)
        }
    }

    private fun checkMaratoneta(esami: List<Esame>): Boolean {
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
