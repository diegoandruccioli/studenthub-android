package com.unibo.android.domain.repository

import com.unibo.android.domain.model.UserStats
import kotlinx.coroutines.flow.Flow
import com.unibo.android.domain.model.LeaderboardEntry

interface GamificationRepository {
    val userStatsFlow: Flow<UserStats>
    val leaderboardFlow: Flow<List<LeaderboardEntry>>
    suspend fun getUserStats(): Result<Unit>
    suspend fun getLeaderboard(): Result<Unit>
}
