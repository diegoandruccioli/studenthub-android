package com.unibo.android.domain.usecase

import com.unibo.android.domain.model.User
import com.unibo.android.domain.repository.AuthRepository

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<User> =
        repository.login(email, password)
}
