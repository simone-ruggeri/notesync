package com.notesync.ui.notes

import com.notesync.data.local.SyncStatus
import com.notesync.data.repository.NoteRepository
import com.notesync.domain.model.Note
import com.notesync.util.MainDispatcherRule
import com.notesync.util.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class NotesViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val mockRepo = mock<NoteRepository>()
    private val mockTokenManager = mock<TokenManager>()
    private val notesFlow = MutableStateFlow<List<Note>>(emptyList())

    private lateinit var viewModel: NotesViewModel

    @Before
    fun setup() {
        whenever(mockRepo.notes).thenReturn(notesFlow)
        whenever(mockTokenManager.emailFlow).thenReturn(flowOf("user@test.com"))
        viewModel = NotesViewModel(mockRepo, mockTokenManager)
    }

    private fun note(id: String, title: String, syncStatus: SyncStatus = SyncStatus.SYNCED) = Note(
        id = id, title = title, content = "Content of $title",
        updatedAt = System.currentTimeMillis(), syncStatus = syncStatus
    )

    @Test
    fun init_collectsNotes_updatesUiState() = runTest(dispatcherRule.testDispatcher) {
        notesFlow.value = listOf(note("1", "Alpha"), note("2", "Beta"))
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.notes.size)
        assertEquals(2, viewModel.uiState.value.allNotesCount)
    }

    @Test
    fun init_collectsEmail_updatesUserEmail() = runTest(dispatcherRule.testDispatcher) {
        advanceUntilIdle()

        assertEquals("user@test.com", viewModel.uiState.value.userEmail)
    }

    @Test
    fun searchQuery_filtersByTitleCaseInsensitive() = runTest(dispatcherRule.testDispatcher) {
        notesFlow.value = listOf(note("1", "Shopping list"), note("2", "Work meeting"))
        advanceUntilIdle()

        viewModel.onSearchQueryChange("shopping")
        advanceUntilIdle()

        val notes = viewModel.uiState.value.notes
        assertEquals(1, notes.size)
        assertEquals("Shopping list", notes[0].title)
    }

    @Test
    fun searchQuery_filtersByContent() = runTest(dispatcherRule.testDispatcher) {
        notesFlow.value = listOf(
            Note("1", "Note A", "meeting agenda", 0L, SyncStatus.SYNCED),
            Note("2", "Note B", "shopping items", 0L, SyncStatus.SYNCED)
        )
        advanceUntilIdle()

        viewModel.onSearchQueryChange("meeting agenda")
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.notes.size)
        assertEquals("Note A", viewModel.uiState.value.notes[0].title)
    }

    @Test
    fun searchQuery_blank_returnsAllNotes() = runTest(dispatcherRule.testDispatcher) {
        notesFlow.value = listOf(note("1", "Alpha"), note("2", "Beta"))
        advanceUntilIdle()
        viewModel.onSearchQueryChange("alpha")
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.notes.size)

        viewModel.onSearchQueryChange("")
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.notes.size)
    }

    @Test
    fun localNotesCount_countsOnlyPendingNotes() = runTest(dispatcherRule.testDispatcher) {
        notesFlow.value = listOf(
            note("1", "A", SyncStatus.PENDING_CREATE),
            note("2", "B", SyncStatus.PENDING_UPDATE),
            note("3", "C", SyncStatus.SYNCED)
        )
        advanceUntilIdle()

        assertEquals(3, viewModel.uiState.value.allNotesCount)
        assertEquals(2, viewModel.uiState.value.localNotesCount)
    }

    @Test
    fun refresh_callsSyncPendingAndRefreshFromServer() = runTest(dispatcherRule.testDispatcher) {
        advanceUntilIdle()
        clearInvocations(mockRepo)

        viewModel.refresh()
        advanceUntilIdle()

        verify(mockRepo).syncPending()
        verify(mockRepo).refreshFromServer()
    }

    @Test
    fun refresh_onNetworkError_setsErrorMessage() = runTest(dispatcherRule.testDispatcher) {
        whenever(mockRepo.refreshFromServer()).thenThrow(RuntimeException("network failure"))
        advanceUntilIdle()
        clearInvocations(mockRepo)

        viewModel.refresh()
        advanceUntilIdle()

        val error = viewModel.uiState.value.error
        assertTrue(error?.contains("Errore sincronizzazione") == true)
    }

    @Test
    fun refresh_setsIsLoadingFalseAfterCompletion() = runTest(dispatcherRule.testDispatcher) {
        advanceUntilIdle()
        clearInvocations(mockRepo)

        viewModel.refresh()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun deleteNote_delegatesToRepository() = runTest(dispatcherRule.testDispatcher) {
        advanceUntilIdle()

        viewModel.deleteNote("note-id-1")
        advanceUntilIdle()

        verify(mockRepo).deleteNote("note-id-1")
    }

    @Test
    fun logout_setsShouldLogoutTrue() = runTest(dispatcherRule.testDispatcher) {
        advanceUntilIdle()

        viewModel.logout()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.shouldLogout)
    }

    @Test
    fun onLogoutHandled_resetsShouldLogout() = runTest(dispatcherRule.testDispatcher) {
        advanceUntilIdle()
        viewModel.logout()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.shouldLogout)

        viewModel.onLogoutHandled()

        assertFalse(viewModel.uiState.value.shouldLogout)
    }

    @Test
    fun clearError_resetsErrorToNull() = runTest(dispatcherRule.testDispatcher) {
        advanceUntilIdle()
        whenever(mockRepo.refreshFromServer()).thenThrow(RuntimeException("fail"))
        viewModel.refresh()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.error != null)

        viewModel.clearError()

        assertNull(viewModel.uiState.value.error)
    }
}
