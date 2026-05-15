package com.unibo.android.domain.usecase

import com.unibo.android.domain.model.Esame
import com.unibo.android.domain.model.ObiettiviIds
import com.unibo.android.domain.repository.EsameRepository
import com.unibo.android.domain.repository.ObiettivoRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.YearMonth

class CheckObiettiviUseCase(
    private val esameRepository: EsameRepository,
    private val obiettivoRepository: ObiettivoRepository,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    suspend operator fun invoke() = withContext(defaultDispatcher) {
        val esami = esameRepository.getEsami().first()

        // 1. Primo Passo: almeno 1 esame
        if (esami.isNotEmpty()) {
            obiettivoRepository.updateGoalCompletion(ObiettiviIds.PRIMO_PASSO, true)
        }

        // 2. Secchione: almeno 1 lode
        if (esami.any { it.lode }) {
            obiettivoRepository.updateGoalCompletion(ObiettiviIds.SECCHIONE, true)
        }

        // 3. Maratoneta: 3 esami in un mese
        if (checkMaratoneta(esami)) {
            obiettivoRepository.updateGoalCompletion(ObiettiviIds.MARATONETA, true)
        }

        // 4. Giro di Boa: 90 CFU
        if (esami.sumOf { it.cfu } >= 90) {
            obiettivoRepository.updateGoalCompletion(ObiettiviIds.GIRO_DI_BOA, true)
        }
    }

    private fun checkMaratoneta(esami: List<Esame>): Boolean {
        val monthYearCounts = mutableMapOf<YearMonth, Int>()

        esami.forEach { esame ->
            val yearMonth = YearMonth.from(esame.dataEsame)
            monthYearCounts[yearMonth] = (monthYearCounts[yearMonth] ?: 0) + 1
        }

        return monthYearCounts.values.any { it >= 3 }
    }
}
