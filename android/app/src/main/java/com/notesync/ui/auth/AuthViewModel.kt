package com.notesync.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notesync.data.repository.AuthRepository
import com.notesync.data.repository.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.login(email, password)) {
                is AuthResult.Success -> _uiState.update {
                    it.copy(isLoading = false, isSuccess = true) }
                is AuthResult.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message) }
            }
        }
    }
    fun register(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.register(email, password)) {
                is AuthResult.Success -> _uiState.update {
                    it.copy(isLoading = false, isSuccess = true) }
                is AuthResult.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message) }
            }
        }
    }
    fun clearError() { _uiState.update { it.copy(error = null) } }
}