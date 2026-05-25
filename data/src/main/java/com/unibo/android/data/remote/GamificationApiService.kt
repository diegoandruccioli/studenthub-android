package com.unibo.android.data.remote

import com.unibo.android.data.remote.dto.BadgeDto
import com.unibo.android.data.remote.dto.GamificationStatusDto
import com.unibo.android.data.remote.dto.LeaderboardResponseDto
import retrofit2.Response
import retrofit2.http.GET

interface GamificationApiService {
    @GET("gamification/status")
    suspend fun getStatus(): Response<GamificationStatusDto>

    @GET("users/leaderboard")
    suspend fun getLeaderboard(): Response<LeaderboardResponseDto>

    @GET("gamification/badges")
    suspend fun getBadges(): Response<List<BadgeDto>>

    @GET("gamification/my-badges")
    suspend fun getMyBadges(): Response<List<BadgeDto>>
}
