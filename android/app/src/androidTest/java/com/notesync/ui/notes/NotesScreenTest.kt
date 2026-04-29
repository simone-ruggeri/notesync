package com.notesync.ui.notes

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.notesync.StubApiService
import com.notesync.data.local.NoteDatabase
import com.notesync.data.local.NoteEntity
import com.notesync.data.local.SyncStatus
import com.notesync.data.repository.NoteRepository
import com.notesync.ui.theme.NoteSyncTheme
import com.notesync.util.NetworkChecker
import com.notesync.util.TokenManager
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotesScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var db: NoteDatabase
    private lateinit var tokenManager: TokenManager
    private lateinit var viewModel: NotesViewModel

    private val offlineChecker = object : NetworkChecker { override fun isOnline() = false }

    @Before
    fun setup(): Unit = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, NoteDatabase::class.java).build()
        tokenManager = TokenManager(context)
        tokenManager.saveToken("test-token")
        tokenManager.saveUserId("uid-test")
        tokenManager.saveEmail("user@test.com")
        tokenManager.initCache()

        val repository = NoteRepository(
            noteDao = db.noteDao(),
            apiService = StubApiService(),
            tokenManager = tokenManager,
            network = offlineChecker
        )
        viewModel = NotesViewModel(repository, tokenManager)
    }

    @After
    fun teardown(): Unit = runBlocking {
        tokenManager.clearToken()
        db.close()
    }

    @Test
    fun emptyState_showsNessunaNota() {
        composeTestRule.setContent {
            NoteSyncTheme {
                NotesScreen(viewModel = viewModel, onNavigateToCreate = {}, onNavigateToEdit = {}, onLogout = {})
            }
        }
        composeTestRule.onNodeWithText("Nessuna nota").assertIsDisplayed()
    }

    @Test
    fun notesLoaded_showsNoteCards() {
        runBlocking {
            db.noteDao().insertNote(NoteEntity("id-1", "srv-1", "uid-test", "Alpha Note", "Content A", 0L, 1L, SyncStatus.SYNCED.name))
            db.noteDao().insertNote(NoteEntity("id-2", "srv-2", "uid-test", "Beta Note", "Content B", 0L, 2L, SyncStatus.SYNCED.name))
        }

        composeTestRule.setContent {
            NoteSyncTheme {
                NotesScreen(viewModel = viewModel, onNavigateToCreate = {}, onNavigateToEdit = {}, onLogout = {})
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodes(androidx.compose.ui.test.hasText("Alpha Note")).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Alpha Note").assertIsDisplayed()
        composeTestRule.onNodeWithText("Beta Note").assertIsDisplayed()
    }

    @Test
    fun syncedNote_showsSincronizzataBadge() {
        runBlocking {
            db.noteDao().insertNote(NoteEntity("id-1", "srv-1", "uid-test", "Synced Note", "", 0L, 1L, SyncStatus.SYNCED.name))
        }

        composeTestRule.setContent {
            NoteSyncTheme {
                NotesScreen(viewModel = viewModel, onNavigateToCreate = {}, onNavigateToEdit = {}, onLogout = {})
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodes(androidx.compose.ui.test.hasText("Sincronizzata")).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Sincronizzata").assertIsDisplayed()
    }

    @Test
    fun pendingNote_showsLocaleBadge() {
        runBlocking {
            db.noteDao().insertNote(NoteEntity("id-1", null, "uid-test", "Local Note", "", 0L, 1L, SyncStatus.PENDING_CREATE.name))
        }

        composeTestRule.setContent {
            NoteSyncTheme {
                NotesScreen(viewModel = viewModel, onNavigateToCreate = {}, onNavigateToEdit = {}, onLogout = {})
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodes(androidx.compose.ui.test.hasText("Locale")).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Locale").assertIsDisplayed()
    }

    @Test
    fun topBar_showsEmailInitial() {
        composeTestRule.setContent {
            NoteSyncTheme {
                NotesScreen(viewModel = viewModel, onNavigateToCreate = {}, onNavigateToEdit = {}, onLogout = {})
            }
        }
        composeTestRule.onNodeWithText("U").assertIsDisplayed()
    }
}
