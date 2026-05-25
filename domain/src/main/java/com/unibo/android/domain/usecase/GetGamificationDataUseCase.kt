package com.unibo.android.domain.usecase

import com.unibo.android.domain.model.UserStats
import com.unibo.android.domain.repository.GamificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Use case to observe the current user's gamification statistics.
 * Purely reactive — observes the SSOT (DataStore).
 * Business logic for levels and XP is managed by the backend.
 */
class GetGamificationDataUseCase(
    private val gamificationRepository: GamificationRepository
) {
    operator fun invoke(): Flow<UserStats> = gamificationRepository.userStatsFlow.map { stats ->
        val xpLabel = if (stats.prossimaSoglia != null) {
            "${stats.xp} / ${stats.prossimaSoglia} XP"
        } else {
            "${stats.xp} XP (MAX)"
        }

        val formattedTitle = "Lv. ${stats.level} - ${stats.levelTitle}"

        stats.copy(
            levelTitle = formattedTitle,
            xpLabel = xpLabel,
            progressPercentage = (stats.progressPercentage / 100f).coerceIn(0f, 1f)
        )
    }
}
