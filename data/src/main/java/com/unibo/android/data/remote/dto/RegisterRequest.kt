package com.unibo.android.data.remote.dto

data class RegisterRequest(
    val nome: String,
    val cognome: String,
    val email: String,
    val password: String
)
