package com.unibo.android.domain.usecase

import com.unibo.android.domain.model.UserStats
import com.unibo.android.domain.repository.EsameRepository
import com.unibo.android.domain.repository.GamificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Use case to observe the current user's gamification statistics.
 * Aggregates local XP from Room with remote status (Rank, Level name) from DataStore.
 */
class GetGamificationDataUseCase(
    private val gamificationRepository: GamificationRepository,
    private val esameRepository: EsameRepository
) {
    operator fun invoke(): Flow<UserStats> = combine(
        gamificationRepository.userStatsFlow,
        esameRepository.totalXpFlow
    ) { stats, localXp ->
        // Calcola la label usando i dati locali per XP e remoti per la soglia
        val xpLabel = if (stats.prossimaSoglia != null) {
            "$localXp / ${stats.prossimaSoglia} XP"
        } else {
            "$localXp XP (MAX)"
        }

        // Formattazione del titolo livello
        val formattedTitle = "Lv. ${stats.level} - ${stats.levelTitle}"

        // Calcolo percentuale progressiva per la barra basata su dati locali
        val percentage = if (stats.prossimaSoglia != null && stats.prossimaSoglia > 0) {
            localXp.toFloat() / stats.prossimaSoglia.toFloat()
        } else {
            stats.progressPercentage / 100f // Fallback al dato server se MAX o errore
        }

        stats.copy(
            xp = localXp,
            levelTitle = formattedTitle,
            xpLabel = xpLabel,
            progressPercentage = percentage.coerceIn(0f, 1f)
        )
    }
}
