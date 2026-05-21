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
        // No local calculations. We just ensure the label is formatted if needed,
        // although ideally the backend should provide the formatted label too.
        // If currentXp and level are directly from server, they are the truth.
        stats
    }
}
