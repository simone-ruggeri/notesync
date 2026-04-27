package com.notesync.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notesync.data.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NoteDetailUiState(
    val title: String = "",
    val content: String = "",
    val isSaving: Boolean = false,
    val error: String? = null,
    val savedSuccessfully: Boolean = false
)

class NoteDetailViewModel(
    private val repository: NoteRepository,
    private val mode: NoteMode
) : ViewModel() {

    private val _uiState = MutableStateFlow(NoteDetailUiState())
    val uiState: StateFlow<NoteDetailUiState> = _uiState.asStateFlow()

    init {
        when (mode) {
            is NoteMode.Edit -> loadNote(mode.noteId)
            NoteMode.Create -> Unit
        }
    }

    private fun loadNote(id: String) {
        viewModelScope.launch {
            val note = repository.getNoteById(id)
            if (note != null) {
                _uiState.update { it.copy(title = note.title, content = note.content) }
            }
        }
    }

    fun onTitleChange(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun onContentChange(content: String) {
        _uiState.update { it.copy(content = content) }
    }

    fun save() {
        val state = _uiState.value
        if (state.title.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                when (mode) {
                    NoteMode.Create -> repository.createNote(state.title, state.content)
                    is NoteMode.Edit -> repository.updateNote(mode.noteId, state.title, state.content)
                }
                _uiState.update { it.copy(isSaving = false, savedSuccessfully = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = "Errore salvataggio: ${e.message}") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
