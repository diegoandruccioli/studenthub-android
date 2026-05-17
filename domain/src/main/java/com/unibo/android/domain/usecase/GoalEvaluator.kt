package com.unibo.android.domain.usecase

import com.unibo.android.domain.model.Esame
import com.unibo.android.domain.model.ObiettiviIds
import java.time.YearMonth

interface GoalEvaluator {
    val goalId: Int
    fun evaluate(esami: List<Esame>): Boolean
}

class PrimoPassoEvaluator : GoalEvaluator {
    override val goalId = ObiettiviIds.PRIMO_PASSO
    override fun evaluate(esami: List<Esame>) = esami.isNotEmpty()
}

class SecchioneEvaluator : GoalEvaluator {
    override val goalId = ObiettiviIds.SECCHIONE
    override fun evaluate(esami: List<Esame>) = esami.any { it.lode }
}

class MaratonetaEvaluator : GoalEvaluator {
    override val goalId = ObiettiviIds.MARATONETA
    override fun evaluate(esami: List<Esame>): Boolean {
        val monthYearCounts = mutableMapOf<YearMonth, Int>()
        esami.forEach { esame ->
            val yearMonth = YearMonth.from(esame.dataEsame)
            monthYearCounts[yearMonth] = (monthYearCounts[yearMonth] ?: 0) + 1
        }
        return monthYearCounts.values.any { it >= 3 }
    }
}

class GiroDiBoaEvaluator : GoalEvaluator {
    override val goalId = ObiettiviIds.GIRO_DI_BOA
    override fun evaluate(esami: List<Esame>) = esami.sumOf { it.cfu } >= 90
}
