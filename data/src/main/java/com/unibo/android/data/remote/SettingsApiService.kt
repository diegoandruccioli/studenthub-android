package com.unibo.android.data.remote

import com.unibo.android.data.remote.dto.SettingsRequest
import com.unibo.android.data.remote.dto.SettingsResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface SettingsApiService {
    @GET("settings")
    suspend fun getSettings(): Response<SettingsResponse>

    @PUT("settings")
    suspend fun updateSettings(@Body body: SettingsRequest): Response<Unit>
}
