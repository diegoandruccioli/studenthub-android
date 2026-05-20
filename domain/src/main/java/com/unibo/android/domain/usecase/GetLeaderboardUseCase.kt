package com.unibo.android.domain.usecase

import com.unibo.android.domain.model.LeaderboardEntry
import com.unibo.android.domain.repository.GamificationRepository

class GetLeaderboardUseCase(private val repository: GamificationRepository) {
    suspend operator fun invoke(): Result<List<LeaderboardEntry>> = repository.getLeaderboard()
}
