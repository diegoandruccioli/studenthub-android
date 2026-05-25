package com.unibo.android.ui.screens.gamification

import com.unibo.android.domain.model.LeaderboardEntry
import com.unibo.android.domain.model.UserStats

/**
 * Represent the UI state for the Gamification screen.
 * Following the Unidirectional Data Flow (UDF) pattern.
 */
sealed class GamificationUiState {
    /**
     * Data is being loaded for the first time.
     */
    object Loading : GamificationUiState()

    /**
     * Data loaded successfully.
     * @property stats The current user's statistics.
     * @property leaderboard The list of leaderboard entries.
     */
    data class Success(
        val stats: UserStats,
        val leaderboard: List<LeaderboardEntry>,
    ) : GamificationUiState()

    /**
     * An error occurred while loading data.
     * @property message The error message to display.
     */
    data class Error(val message: String) : GamificationUiState()
}
