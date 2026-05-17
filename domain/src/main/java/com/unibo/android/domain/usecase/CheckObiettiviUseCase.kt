package com.unibo.android.domain.usecase

import com.unibo.android.domain.repository.EsameRepository
import com.unibo.android.domain.repository.ObiettivoRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class CheckObiettiviUseCase(
    private val esameRepository: EsameRepository,
    private val obiettivoRepository: ObiettivoRepository,
    private val evaluators: List<GoalEvaluator> = listOf(
        PrimoPassoEvaluator(),
        SecchioneEvaluator(),
        MaratonetaEvaluator(),
        GiroDiBoaEvaluator()
    ),
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    suspend operator fun invoke() = withContext(defaultDispatcher) {
        val esami = esameRepository.getEsami().first()

        evaluators.forEach { evaluator ->
            if (evaluator.evaluate(esami)) {
                obiettivoRepository.updateGoalCompletion(evaluator.goalId, true)
            }
        }
    }
}
