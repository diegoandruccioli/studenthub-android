package com.unibo.android.domain.usecase

import com.unibo.android.domain.model.LeaderboardEntry
import com.unibo.android.domain.repository.GamificationRepository
import kotlinx.coroutines.flow.Flow

class GetLeaderboardUseCase(private val repository: GamificationRepository) {
    operator fun invoke(): Flow<List<LeaderboardEntry>> = repository.leaderboardFlow
}
