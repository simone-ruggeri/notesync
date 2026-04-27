package com.notesync.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notesync.data.repository.NoteRepository
import com.notesync.domain.model.Note
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotesUiState(
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val shouldLogout: Boolean = false
)

// ViewModel standard: nessuna annotazione Koin nelle classi di presentazione.
// Dichiarato nel modulo con: viewModel { NotesViewModel(get()) }
class NotesViewModel(private val repository: NoteRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.notes.collect { notes ->
                _uiState.update { it.copy(notes = notes) }
            }
        }
        refresh()
    }

    fun deleteNote(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteNote(id)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Errore eliminazione: " +
                                e.message
                    )
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                repository.syncPending()
                repository.refreshFromServer()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Errore sincronizzazione: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _uiState.update { it.copy(shouldLogout = true) }
        }
    }

    fun onLogoutHandled() {
        _uiState.update { it.copy(shouldLogout = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

}