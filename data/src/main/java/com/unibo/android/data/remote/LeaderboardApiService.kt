package com.unibo.android.data.remote

import retrofit2.http.GET

interface LeaderboardApiService {
    @GET("/api/users/leaderboard")
    suspend fun getLeaderboard(): List<LeaderboardResponseDto>
}
