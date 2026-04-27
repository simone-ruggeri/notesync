package com.notesync.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences>
        by preferencesDataStore(name = "user_prefs")

class TokenManager(private val context: Context) {
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("jwt_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
    }

    // Cache in memoria: AuthInterceptor legge qui in modo sincrono,
    // senza runBlocking su DataStore a ogni richiesta.
    @Volatile private var _cachedToken: String? = null

    fun getCachedToken(): String? = _cachedToken

    val userIdFlow: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[USER_ID_KEY] }

    suspend fun saveToken(token: String) {
        _cachedToken = token
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }

    suspend fun saveUserId(userId: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
        }
    }

    suspend fun getToken(): String? {
        return context.dataStore.data
            .map { preferences -> preferences[TOKEN_KEY] }
            .first()
    }

    suspend fun getUserId(): String? {
        return context.dataStore.data
            .map { preferences -> preferences[USER_ID_KEY] }
            .first()
    }

    suspend fun clearToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
            preferences.remove(USER_ID_KEY)
        }
        _cachedToken = null
    }

    suspend fun isLoggedIn(): Boolean = getToken() != null

    // Chiamato all'avvio dell'app per precaricare il token dalle sessioni precedenti.
    suspend fun initCache() {
        _cachedToken = getToken()
    }
}
