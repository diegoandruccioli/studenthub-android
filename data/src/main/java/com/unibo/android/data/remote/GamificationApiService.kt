package com.unibo.android.data.remote

import com.unibo.android.data.remote.dto.GamificationStatusDto
import com.unibo.android.data.remote.dto.LeaderboardEntryDto
import retrofit2.Response
import retrofit2.http.GET

interface GamificationApiService {
    @GET("gamification/status")
    suspend fun getStatus(): Response<GamificationStatusDto>

    @GET("gamification/leaderboard")
    suspend fun getLeaderboard(): Response<List<LeaderboardEntryDto>>
}
