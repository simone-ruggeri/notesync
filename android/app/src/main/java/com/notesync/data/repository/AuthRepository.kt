package com.notesync.data.repository

import com.notesync.data.remote.ApiService
import com.notesync.data.remote.dto.AuthRequest
import com.notesync.util.TokenManager

sealed class AuthResult<out T> {
    data class Success<T>(val data: T) : AuthResult<T>()
    data class Error(val message: String) : AuthResult<Nothing>()
}

class AuthRepository(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {
    suspend fun register(email: String, password: String): AuthResult<Unit> {
        return try {
            val response = apiService.register(AuthRequest(email, password))
            if (response.isSuccessful) {
                val body = response.body()!!
                tokenManager.saveToken(body.token)
                tokenManager.saveUserId(body.userId)
                AuthResult.Success(Unit)
            } else {
                val errorMsg = when (response.code()) {
                    409 -> "Email già registrata"
                    400 -> "Dati non validi"
                    else -> "Errore del server (codice HTTP)"
                }
                AuthResult.Error(errorMsg)
            }
        } catch (e: Exception) {
            AuthResult.Error("Errore di rete: " + e.message)
        }
    }

    suspend fun login(email: String, password: String): AuthResult<Unit> {
        return try {
            val response = apiService.login(AuthRequest(email, password))
            if (response.isSuccessful) {
                val body = response.body()!!
                tokenManager.saveToken(body.token)
                tokenManager.saveUserId(body.userId)
                AuthResult.Success(Unit)
            } else {
                AuthResult.Error(
                    if (response.code() == 401) "Credenziali non valide"
                    else "Errore del server"
                )
            }
        } catch (e: Exception) {
            AuthResult.Error("Errore di rete: " + e.message)
        }
    }

    suspend fun logout() {
        tokenManager.clearToken()
    }

    suspend fun isLoggedIn(): Boolean = tokenManager.isLoggedIn()
}