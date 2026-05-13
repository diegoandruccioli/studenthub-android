package com.unibo.android.data.remote

import com.unibo.android.data.remote.dto.AuthResponse
import com.unibo.android.data.remote.dto.LoginRequest
import com.unibo.android.data.remote.dto.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<Unit>
}
