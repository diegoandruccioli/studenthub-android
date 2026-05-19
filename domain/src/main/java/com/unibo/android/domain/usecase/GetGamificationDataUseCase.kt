package com.unibo.android.domain.usecase

import com.unibo.android.domain.model.LeaderboardEntry
import com.unibo.android.domain.model.UserStats
import com.unibo.android.domain.repository.GamificationRepository

class GetGamificationDataUseCase(
    private val repository: GamificationRepository
) {
    suspend fun getLeaderboard(): Result<List<LeaderboardEntry>> {
        return repository.getLeaderboard()
    }

    suspend fun getUserStats(): Result<UserStats> {
        return repository.getUserStats()
    }
}
