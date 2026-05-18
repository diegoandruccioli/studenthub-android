package com.unibo.android.data.remote.dto

data class AuthResponse(
    val success: Boolean,
    val user: UserDto?
)

data class UserDto(
    val id: Int,
    val nome: String,
    val cognome: String,
    val email: String,
    val ruolo: String
)

data class ErrorResponse(
    val error: String?,
    val message: String?
)

data class RefreshTokenResponse(val success: Boolean)
