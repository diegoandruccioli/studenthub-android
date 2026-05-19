package com.unibo.android.domain.repository

import com.unibo.android.domain.model.LeaderboardEntry
import com.unibo.android.domain.model.UserStats

interface GamificationRepository {
    suspend fun getLeaderboard(): Result<List<LeaderboardEntry>>
    suspend fun getUserStats(): Result<UserStats>
}
