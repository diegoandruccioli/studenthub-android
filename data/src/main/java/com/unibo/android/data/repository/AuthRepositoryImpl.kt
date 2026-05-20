package com.unibo.android.data.repository

import android.content.Context
import com.google.gson.Gson
import com.unibo.android.data.local.SessionDataStore
import com.unibo.android.data.remote.NetworkClient
import com.unibo.android.data.remote.dto.ErrorResponse
import com.unibo.android.data.remote.dto.LoginRequest
import com.unibo.android.data.remote.dto.RegisterRequest
import com.unibo.android.domain.model.User
import com.unibo.android.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AuthRepositoryImpl(context: Context) : AuthRepository {

    private val apiService = NetworkClient.authApiService
    private val sessionDataStore = SessionDataStore(context)
    private val gson = Gson()

    override suspend fun login(email: String, password: String): Result<User> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.login(LoginRequest(email, password))
                if (response.isSuccessful) {
                    val body = response.body()
                    val userDto = body?.user
                    if (body?.success == true && userDto != null) {
                        sessionDataStore.setLoggedIn(true)
                        sessionDataStore.setUserId(userDto.id)
                        Result.success(
                            User(userDto.id, userDto.nome, userDto.cognome, userDto.email, userDto.ruolo)
                        )
                    } else {
                        Result.failure(Exception("Credenziali non valide"))
                    }
                } else {
                    val errorMsg = response.errorBody()?.string()
                        ?.let { runCatching { gson.fromJson(it, ErrorResponse::class.java) }.getOrNull() }
                        ?.let { it.error ?: it.message }
                        ?: "Credenziali non valide"
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Impossibile raggiungere il server"))
            }
        }

    override suspend fun register(
        nome: String,
        cognome: String,
        email: String,
        password: String
    ): Result<User> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.register(RegisterRequest(nome, cognome, email, password))
            if (response.isSuccessful) {
                val body = response.body()
                val userDto = body?.user
                if (body?.success == true && userDto != null) {
                    sessionDataStore.setLoggedIn(true)
                    sessionDataStore.setUserId(userDto.id)
                    Result.success(
                        User(userDto.id, userDto.nome, userDto.cognome, userDto.email, userDto.ruolo)
                    )
                } else {
                    Result.failure(Exception("Registrazione fallita"))
                }
            } else {
                val errorMsg = response.errorBody()?.string()
                    ?.let { runCatching { gson.fromJson(it, ErrorResponse::class.java) }.getOrNull() }
                    ?.let { it.error ?: it.message }
                    ?: "Registrazione fallita"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Impossibile raggiungere il server"))
        }
    }

    override suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching { apiService.logout() }
        NetworkClient.clearCookies()
        sessionDataStore.setLoggedIn(false)
        Result.success(Unit)
    }

    override fun isLoggedIn(): Flow<Boolean> = sessionDataStore.isLoggedIn
}
