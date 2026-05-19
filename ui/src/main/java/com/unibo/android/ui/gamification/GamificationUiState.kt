package com.unibo.android.ui.gamification

import com.unibo.android.domain.model.LeaderboardEntry
import com.unibo.android.domain.model.UserStats

sealed interface GamificationUiState {
    data object Loading : GamificationUiState
    data class Success(
        val userStats: UserStats,
        val leaderboard: List<LeaderboardEntry>
    ) : GamificationUiState
    data class Error(val message: String) : GamificationUiState
}
