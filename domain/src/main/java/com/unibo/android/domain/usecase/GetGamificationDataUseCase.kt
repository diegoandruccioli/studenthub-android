package com.unibo.android.domain.usecase

import com.unibo.android.domain.model.UserStats
import com.unibo.android.domain.repository.GamificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Use case to observe the current user's gamification statistics.
 * This is purely reactive and observes the SSOT (DataStore).
 * Business logic for levels and XP is managed by the backend.
 */
class GetGamificationDataUseCase(
    private val gamificationRepository: GamificationRepository
) {
    operator fun invoke(): Flow<UserStats> = gamificationRepository.userStatsFlow.map { stats ->
        // Utilizziamo ESCLUSIVAMENTE la Truth fornita dal back end e salvata nel DataStore
        val xpLabel = if (stats.prossimaSoglia != null) {
            "${stats.xp} / ${stats.prossimaSoglia} XP"
        } else {
            "${stats.xp} XP (MAX)"
        }

        // Formattazione del titolo livello
        val formattedTitle = "Lv. ${stats.level} - ${stats.levelTitle}"

        stats.copy(
            levelTitle = formattedTitle,
            xpLabel = xpLabel,
            // Il backend invia una percentuale int (0-100), la UI in Compose richiede un float (0.0-1.0)
            progressPercentage = (stats.progressPercentage / 100f).coerceIn(0f, 1f)
        )
    }
}
