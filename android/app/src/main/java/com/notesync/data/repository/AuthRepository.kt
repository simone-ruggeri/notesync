package com.notesync.data.repository

import com.notesync.data.remote.ApiService
import com.notesync.data.remote.dto.AuthRequest
import com.notesync.util.TokenManager

/** Risultato di un'operazione di autenticazione (login o registrazione). */
sealed class AuthResult<out T> {
    /** Operazione riuscita. [data] contiene il valore restituito (Unit per login/register). */
    data class Success<T>(val data: T) : AuthResult<T>()
    /** Operazione fallita. [message] è un messaggio leggibile da mostrare all'utente. */
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
                val body = response.body() ?: return AuthResult.Error("Risposta vuota dal server")
                tokenManager.saveToken(body.token)
                tokenManager.saveUserId(body.userId)
                tokenManager.saveEmail(email)
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
                val body = response.body() ?: return AuthResult.Error("Risposta vuota dal server")
                tokenManager.saveToken(body.token)
                tokenManager.saveUserId(body.userId)
                tokenManager.saveEmail(email)
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

}