package com.unibo.android.domain.usecase

import com.unibo.android.domain.model.User
import com.unibo.android.domain.repository.AuthRepository

class RegisterUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(
        nome: String,
        cognome: String,
        email: String,
        password: String
    ): Result<User> = repository.register(nome, cognome, email, password)
}
