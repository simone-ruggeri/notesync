package com.notesync.ui.notes

import com.notesync.data.local.NoteEntity
import com.notesync.data.local.SyncStatus
import com.notesync.data.repository.NoteRepository
import com.notesync.domain.model.Note
import com.notesync.util.MainDispatcherRule
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class NoteDetailViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val mockRepo = mock<NoteRepository>()

    private fun createNote(id: String = "id-1", title: String = "My Note", content: String = "Body") = Note(
        id = id, title = title, content = content,
        updatedAt = 1000L, syncStatus = SyncStatus.SYNCED
    )

    private fun createEntity(id: String = "id-1", title: String = "My Note") = NoteEntity(
        id = id, serverId = "srv-1", userId = "uid",
        title = title, content = "Body",
        createdAt = 0L, updatedAt = 0L,
        syncStatus = SyncStatus.PENDING_CREATE.name
    )

    @Test
    fun createMode_init_stateIsEmpty() = runTest(dispatcherRule.testDispatcher) {
        val viewModel = NoteDetailViewModel(mockRepo, NoteMode.Create)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("", state.title)
        assertEquals("", state.content)
        assertFalse(state.savedSuccessfully)
        verify(mockRepo, never()).getNoteById(any())
    }

    @Test
    fun editMode_init_loadsNoteFromRepository() = runTest(dispatcherRule.testDispatcher) {
        whenever(mockRepo.getNoteById("id-1")).thenReturn(createNote())
        val viewModel = NoteDetailViewModel(mockRepo, NoteMode.Edit("id-1"))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("My Note", state.title)
        assertEquals("Body", state.content)
        assertEquals(SyncStatus.SYNCED, state.syncStatus)
    }

    @Test
    fun editMode_noteNotFound_stateRemainsEmpty() = runTest(dispatcherRule.testDispatcher) {
        whenever(mockRepo.getNoteById("missing")).thenReturn(null)
        val viewModel = NoteDetailViewModel(mockRepo, NoteMode.Edit("missing"))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("", state.title)
        assertEquals("", state.content)
    }

    @Test
    fun save_blankTitle_doesNotCallRepository() = runTest(dispatcherRule.testDispatcher) {
        val viewModel = NoteDetailViewModel(mockRepo, NoteMode.Create)

        viewModel.save()
        advanceUntilIdle()

        verify(mockRepo, never()).createNote(any(), any())
        verify(mockRepo, never()).updateNote(any(), any(), any())
    }

    @Test
    fun save_createMode_callsCreateNote_setsSavedSuccessfully() = runTest(dispatcherRule.testDispatcher) {
        whenever(mockRepo.createNote("Titolo", "Body")).thenReturn(createEntity())
        val viewModel = NoteDetailViewModel(mockRepo, NoteMode.Create)
        viewModel.onTitleChange("Titolo")
        viewModel.onContentChange("Body")

        viewModel.save()
        advanceUntilIdle()

        verify(mockRepo).createNote("Titolo", "Body")
        assertTrue(viewModel.uiState.value.savedSuccessfully)
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun save_editMode_callsUpdateNote_setsSavedSuccessfully() = runTest(dispatcherRule.testDispatcher) {
        whenever(mockRepo.getNoteById("id-1")).thenReturn(createNote())
        val viewModel = NoteDetailViewModel(mockRepo, NoteMode.Edit("id-1"))
        advanceUntilIdle()
        viewModel.onTitleChange("Nuovo Titolo")
        viewModel.onContentChange("Nuovo Body")

        viewModel.save()
        advanceUntilIdle()

        verify(mockRepo).updateNote("id-1", "Nuovo Titolo", "Nuovo Body")
        assertTrue(viewModel.uiState.value.savedSuccessfully)
    }

    @Test
    fun save_onRepositoryException_setsErrorState() = runTest(dispatcherRule.testDispatcher) {
        whenever(mockRepo.createNote(any(), any())).thenThrow(RuntimeException("DB error"))
        val viewModel = NoteDetailViewModel(mockRepo, NoteMode.Create)
        viewModel.onTitleChange("Titolo")

        viewModel.save()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.error?.contains("Errore salvataggio") == true)
        assertFalse(state.isSaving)
        assertFalse(state.savedSuccessfully)
    }

    @Test
    fun clearError_resetsErrorField() = runTest(dispatcherRule.testDispatcher) {
        whenever(mockRepo.createNote(any(), any())).thenThrow(RuntimeException("fail"))
        val viewModel = NoteDetailViewModel(mockRepo, NoteMode.Create)
        viewModel.onTitleChange("T")
        viewModel.save()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.error != null)

        viewModel.clearError()

        assertNull(viewModel.uiState.value.error)
    }
}
