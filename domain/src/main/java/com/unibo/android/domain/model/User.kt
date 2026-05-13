package com.unibo.android.domain.model

data class User(
    val id: Int,
    val nome: String,
    val cognome: String,
    val email: String,
    val ruolo: String
)
