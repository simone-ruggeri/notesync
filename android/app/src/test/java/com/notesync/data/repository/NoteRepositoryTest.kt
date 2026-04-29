package com.notesync.data.repository

import com.notesync.data.local.NoteDao
import com.notesync.data.local.NoteEntity
import com.notesync.data.local.SyncStatus
import com.notesync.data.remote.ApiService
import com.notesync.data.remote.dto.NoteDto
import com.notesync.util.MainDispatcherRule
import com.notesync.util.NetworkChecker
import com.notesync.util.TokenManager
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import retrofit2.Response
import java.io.IOException

class NoteRepositoryTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val noteDao = mock<NoteDao>()
    private val apiService = mock<ApiService>()
    private val tokenManager = mock<TokenManager>()
    private val fakeNetwork = FakeNetworkChecker(online = true)

    private lateinit var repository: NoteRepository

    private class FakeNetworkChecker(var online: Boolean) : NetworkChecker {
        override fun isOnline() = online
    }

    private fun entity(
        id: String = "local-id",
        serverId: String? = null,
        syncStatus: SyncStatus = SyncStatus.PENDING_CREATE
    ) = NoteEntity(
        id = id, serverId = serverId, userId = "uid-1",
        title = "Title", content = "Body",
        createdAt = 0L, updatedAt = 0L,
        syncStatus = syncStatus.name
    )

    private fun noteDto(id: String = "srv-1") = NoteDto(
        id = id, userId = "uid-1",
        title = "Title", content = "Body",
        createdAt = 0L, updatedAt = 0L
    )

    @Before
    fun setup() = runBlocking {
        whenever(tokenManager.getUserId()).thenReturn("uid-1")
        whenever(tokenManager.userIdFlow).thenReturn(flowOf("uid-1"))
        whenever(noteDao.getAllNotes(any())).thenReturn(flowOf(emptyList()))
        repository = NoteRepository(noteDao, apiService, tokenManager, fakeNetwork)
    }

    // --- createNote ---

    @Test
    fun createNote_offline_insertsPendingCreate_doesNotCallApi() = runTest {
        fakeNetwork.online = false

        repository.createNote("Title", "Body")

        val captor = argumentCaptor<NoteEntity>()
        verify(noteDao).insertNote(captor.capture())
        assertEquals(SyncStatus.PENDING_CREATE.name, captor.firstValue.syncStatus)
        verify(apiService, never()).createNote(any())
    }

    @Test
    fun createNote_online_insertsThenSyncsWithServer() = runTest {
        fakeNetwork.online = true
        whenever(apiService.createNote(any())).thenReturn(Response.success(noteDto("srv-1")))

        repository.createNote("Title", "Body")

        val insertCaptor = argumentCaptor<NoteEntity>()
        verify(noteDao).insertNote(insertCaptor.capture())
        val inserted = insertCaptor.firstValue

        val updateCaptor = argumentCaptor<NoteEntity>()
        verify(noteDao).updateNote(updateCaptor.capture())
        val updated = updateCaptor.firstValue
        assertEquals("srv-1", updated.serverId)
        assertEquals(SyncStatus.SYNCED.name, updated.syncStatus)
        assertEquals(inserted.id, updated.id)
    }

    @Test
    fun createNote_online_apiError_remainsPendingCreate() = runTest {
        fakeNetwork.online = true
        whenever(apiService.createNote(any())).thenReturn(Response.error(500, "".toResponseBody()))

        repository.createNote("Title", "Body")

        verify(noteDao).insertNote(any())
        verify(noteDao, never()).updateNote(any())
    }

    // --- updateNote ---

    @Test
    fun updateNote_offline_updatesPendingUpdate_noApiCall() = runTest {
        fakeNetwork.online = false
        whenever(noteDao.getNoteById("id-1")).thenReturn(entity("id-1", serverId = "srv-1", syncStatus = SyncStatus.SYNCED))

        repository.updateNote("id-1", "New Title", "New Body")

        val captor = argumentCaptor<NoteEntity>()
        verify(noteDao).updateNote(captor.capture())
        assertEquals(SyncStatus.PENDING_UPDATE.name, captor.firstValue.syncStatus)
        verify(apiService, never()).updateNote(any(), any())
    }

    @Test
    fun updateNote_online_patchesServerAndMarksSynced() = runTest {
        fakeNetwork.online = true
        whenever(noteDao.getNoteById("id-1")).thenReturn(entity("id-1", serverId = "srv-1", syncStatus = SyncStatus.SYNCED))
        whenever(apiService.updateNote(any(), any())).thenReturn(Response.success(noteDto("srv-1")))

        repository.updateNote("id-1", "Updated", "New Body")

        val captor = argumentCaptor<NoteEntity>()
        // First call: set PENDING_UPDATE; second call: set SYNCED after sync
        val calls = captor.run {
            verify(noteDao, times(2)).updateNote(capture())
            allValues
        }
        assertEquals(SyncStatus.PENDING_UPDATE.name, calls[0].syncStatus)
        assertEquals(SyncStatus.SYNCED.name, calls[1].syncStatus)
    }

    @Test
    fun updateNote_noteNotFound_doesNothing() = runTest {
        whenever(noteDao.getNoteById("missing")).thenReturn(null)

        repository.updateNote("missing", "T", "B")

        verify(noteDao, never()).updateNote(any())
        verify(apiService, never()).updateNote(any(), any())
    }

    // --- deleteNote ---

    @Test
    fun deleteNote_online_withServerId_apiSuccess_deletesLocally() = runTest {
        fakeNetwork.online = true
        val existing = entity("id-1", serverId = "srv-1", syncStatus = SyncStatus.SYNCED)
        whenever(noteDao.getNoteById("id-1")).thenReturn(existing)
        whenever(apiService.deleteNote("srv-1")).thenReturn(Response.success(Unit))

        repository.deleteNote("id-1")

        verify(noteDao).deleteNote(existing)
        verify(noteDao, never()).updateNote(any())
    }

    @Test
    fun deleteNote_online_withServerId_apiFailure_marksPendingDelete() = runTest {
        fakeNetwork.online = true
        val existing = entity("id-1", serverId = "srv-1", syncStatus = SyncStatus.SYNCED)
        whenever(noteDao.getNoteById("id-1")).thenReturn(existing)
        whenever(apiService.deleteNote(any())).thenThrow(RuntimeException("network error"))

        repository.deleteNote("id-1")

        val captor = argumentCaptor<NoteEntity>()
        verify(noteDao).updateNote(captor.capture())
        assertEquals(SyncStatus.PENDING_DELETE.name, captor.firstValue.syncStatus)
        verify(noteDao, never()).deleteNote(any())
    }

    @Test
    fun deleteNote_online_nullServerId_deletesLocallyWithoutApi() = runTest {
        fakeNetwork.online = true
        val existing = entity("id-1", serverId = null, syncStatus = SyncStatus.PENDING_CREATE)
        whenever(noteDao.getNoteById("id-1")).thenReturn(existing)

        repository.deleteNote("id-1")

        verify(noteDao).deleteNote(existing)
        verify(apiService, never()).deleteNote(any())
    }

    @Test
    fun deleteNote_offline_withServerId_marksPendingDelete() = runTest {
        fakeNetwork.online = false
        val existing = entity("id-1", serverId = "srv-1", syncStatus = SyncStatus.SYNCED)
        whenever(noteDao.getNoteById("id-1")).thenReturn(existing)

        repository.deleteNote("id-1")

        val captor = argumentCaptor<NoteEntity>()
        verify(noteDao).updateNote(captor.capture())
        assertEquals(SyncStatus.PENDING_DELETE.name, captor.firstValue.syncStatus)
        verify(noteDao, never()).deleteNote(any())
    }

    @Test
    fun deleteNote_offline_nullServerId_deletesImmediately() = runTest {
        fakeNetwork.online = false
        val existing = entity("id-1", serverId = null, syncStatus = SyncStatus.PENDING_CREATE)
        whenever(noteDao.getNoteById("id-1")).thenReturn(existing)

        repository.deleteNote("id-1")

        verify(noteDao).deleteNote(existing)
        verify(noteDao, never()).updateNote(any())
    }

    // --- syncPending ---

    @Test
    fun syncPending_offline_doesNothing() = runTest {
        fakeNetwork.online = false

        repository.syncPending()

        verify(noteDao, never()).getPendingSync(any())
    }

    @Test
    fun syncPending_processesPendingCreate() = runTest {
        fakeNetwork.online = true
        val pending = entity("id-1", serverId = null, syncStatus = SyncStatus.PENDING_CREATE)
        whenever(noteDao.getPendingSync("uid-1")).thenReturn(listOf(pending))
        whenever(apiService.createNote(any())).thenReturn(Response.success(noteDto("srv-new")))

        repository.syncPending()

        val captor = argumentCaptor<NoteEntity>()
        verify(noteDao).updateNote(captor.capture())
        assertEquals("srv-new", captor.firstValue.serverId)
        assertEquals(SyncStatus.SYNCED.name, captor.firstValue.syncStatus)
    }

    @Test
    fun syncPending_processesPendingUpdate() = runTest {
        fakeNetwork.online = true
        val pending = entity("id-1", serverId = "srv-1", syncStatus = SyncStatus.PENDING_UPDATE)
        whenever(noteDao.getPendingSync("uid-1")).thenReturn(listOf(pending))
        whenever(apiService.updateNote(any(), any())).thenReturn(Response.success(noteDto("srv-1")))

        repository.syncPending()

        val captor = argumentCaptor<NoteEntity>()
        verify(noteDao).updateNote(captor.capture())
        assertEquals(SyncStatus.SYNCED.name, captor.firstValue.syncStatus)
    }

    @Test
    fun syncPending_processesPendingDelete_success_deletesFromDb() = runTest {
        fakeNetwork.online = true
        val pending = entity("id-1", serverId = "srv-1", syncStatus = SyncStatus.PENDING_DELETE)
        whenever(noteDao.getPendingSync("uid-1")).thenReturn(listOf(pending))
        whenever(apiService.deleteNote("srv-1")).thenReturn(Response.success(Unit))

        repository.syncPending()

        verify(noteDao).deleteNote(pending)
    }

    @Test
    fun syncPending_processesPendingDelete_apiFailure_keepsInDb() = runTest {
        fakeNetwork.online = true
        val pending = entity("id-1", serverId = "srv-1", syncStatus = SyncStatus.PENDING_DELETE)
        whenever(noteDao.getPendingSync("uid-1")).thenReturn(listOf(pending))
        whenever(apiService.deleteNote(any())).thenThrow(RuntimeException("fail"))

        repository.syncPending()

        verify(noteDao, never()).deleteNote(any())
    }

    // --- refreshFromServer ---

    @Test
    fun refreshFromServer_offline_doesNothing() = runTest {
        fakeNetwork.online = false

        repository.refreshFromServer()

        verify(apiService, never()).getNotes()
    }

    @Test
    fun refreshFromServer_insertsServerNotesWithSyncedStatus() = runTest {
        fakeNetwork.online = true
        val dto = noteDto("srv-1")
        whenever(apiService.getNotes()).thenReturn(Response.success(listOf(dto)))
        whenever(noteDao.getNoteByServerId("srv-1")).thenReturn(null)

        repository.refreshFromServer()

        val captor = argumentCaptor<List<NoteEntity>>()
        verify(noteDao).insertAll(captor.capture())
        val inserted = captor.firstValue
        assertEquals(1, inserted.size)
        assertEquals("srv-1", inserted[0].serverId)
        assertEquals(SyncStatus.SYNCED.name, inserted[0].syncStatus)
    }

    @Test
    fun refreshFromServer_preservesLocalIdForExistingNote() = runTest {
        fakeNetwork.online = true
        val dto = noteDto("srv-1")
        val existingLocal = entity("local-uuid", serverId = "srv-1", syncStatus = SyncStatus.SYNCED)
        whenever(apiService.getNotes()).thenReturn(Response.success(listOf(dto)))
        whenever(noteDao.getNoteByServerId("srv-1")).thenReturn(existingLocal)

        repository.refreshFromServer()

        val captor = argumentCaptor<List<NoteEntity>>()
        verify(noteDao).insertAll(captor.capture())
        assertEquals("local-uuid", captor.firstValue[0].id)
    }

    @Test
    fun refreshFromServer_serverError_throwsIOException() = runTest {
        fakeNetwork.online = true
        whenever(apiService.getNotes()).thenReturn(Response.error(401, "".toResponseBody()))

        try {
            repository.refreshFromServer()
            assert(false) { "Expected IOException" }
        } catch (e: IOException) {
            assert(e.message?.contains("401") == true)
        }
    }
}
