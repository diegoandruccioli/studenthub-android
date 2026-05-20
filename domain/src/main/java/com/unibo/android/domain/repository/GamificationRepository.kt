package com.unibo.android.domain.repository

import com.unibo.android.domain.model.LeaderboardEntry
import com.unibo.android.domain.model.UserStats
import kotlinx.coroutines.flow.Flow

interface GamificationRepository {
    val userStatsFlow: Flow<UserStats>
    val leaderboardFlow: Flow<List<LeaderboardEntry>>
    suspend fun getUserStats(): Result<Unit>
    suspend fun getLeaderboard(): Result<List<LeaderboardEntry>>
}
