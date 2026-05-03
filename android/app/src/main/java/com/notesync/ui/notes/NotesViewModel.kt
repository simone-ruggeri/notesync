package com.notesync.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notesync.data.local.SyncStatus
import com.notesync.data.repository.NoteRepository
import com.notesync.domain.model.Note
import com.notesync.util.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotesUiState(
    val notes: List<Note> = emptyList(),
    val allNotesCount: Int = 0,
    val localNotesCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val shouldLogout: Boolean = false,
    val searchQuery: String = "",
    val userEmail: String = ""
)

class NotesViewModel(
    private val repository: NoteRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _allNotes = MutableStateFlow<List<Note>>(emptyList())
    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.notes.collect { notes ->
                _allNotes.value = notes
                applyFilter()
            }
        }
        viewModelScope.launch {
            tokenManager.emailFlow.collect { email ->
                _uiState.update { it.copy(userEmail = email ?: "") }
            }
        }
        refresh()
    }

    // Filtra _allNotes in base alla searchQuery corrente e aggiorna uiState.
    // Chiamata sia quando arrivano nuove note dal DB, sia quando cambia la query.
    private fun applyFilter() {
        val query = _uiState.value.searchQuery
        val all = _allNotes.value
        val filtered = if (query.isBlank()) all
        else all.filter { note ->
            note.title.contains(query, ignoreCase = true) ||
                    note.content.contains(query, ignoreCase = true)
        }
        _uiState.update {
            it.copy(
                notes = filtered,
                allNotesCount = all.size,
                localNotesCount = all.count { n -> n.syncStatus != SyncStatus.SYNCED }
            )
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilter()
    }

    fun deleteNote(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteNote(id)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Errore eliminazione: ${e.message}") }
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
