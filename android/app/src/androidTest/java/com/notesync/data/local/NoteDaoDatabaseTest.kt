package com.notesync.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoteDaoDatabaseTest {

    private lateinit var db: NoteDatabase
    private lateinit var dao: NoteDao

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, NoteDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.noteDao()
    }

    @After
    fun closeDb() = db.close()

    private fun entity(
        id: String,
        title: String = "Title $id",
        userId: String = "uid-1",
        serverId: String? = "srv-$id",
        syncStatus: SyncStatus = SyncStatus.SYNCED,
        updatedAt: Long = System.currentTimeMillis()
    ) = NoteEntity(
        id = id, serverId = serverId, userId = userId,
        title = title, content = "Content $id",
        createdAt = 0L, updatedAt = updatedAt,
        syncStatus = syncStatus.name
    )

    @Test
    fun insertNote_andGetById_returnsEntity() = runTest {
        val note = entity("id-1", title = "My Note")
        dao.insertNote(note)

        val result = dao.getNoteById("id-1")

        assertNotNull(result)
        assertEquals("My Note", result!!.title)
        assertEquals("uid-1", result.userId)
        assertEquals("srv-id-1", result.serverId)
    }

    @Test
    fun getAllNotes_excludesPendingDeleteEntities() = runTest {
        dao.insertNote(entity("id-1", syncStatus = SyncStatus.SYNCED))
        dao.insertNote(entity("id-2", syncStatus = SyncStatus.PENDING_DELETE))

        dao.getAllNotes("uid-1").test {
            val notes = awaitItem()
            assertEquals(1, notes.size)
            assertEquals("id-1", notes[0].id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAllNotes_excludesOtherUsersNotes() = runTest {
        dao.insertNote(entity("id-1", userId = "uid-1"))
        dao.insertNote(entity("id-2", userId = "uid-2"))

        dao.getAllNotes("uid-1").test {
            val notes = awaitItem()
            assertEquals(1, notes.size)
            assertEquals("uid-1", notes[0].userId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAllNotes_orderedByUpdatedAtDesc() = runTest {
        dao.insertNote(entity("id-1", updatedAt = 100L))
        dao.insertNote(entity("id-2", updatedAt = 300L))
        dao.insertNote(entity("id-3", updatedAt = 200L))

        dao.getAllNotes("uid-1").test {
            val notes = awaitItem()
            assertEquals("id-2", notes[0].id)
            assertEquals("id-3", notes[1].id)
            assertEquals("id-1", notes[2].id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun insertAll_withConflict_replacesExistingEntity() = runTest {
        dao.insertNote(entity("id-1", title = "Original"))
        dao.insertAll(listOf(entity("id-1", title = "Replaced")))

        val result = dao.getNoteById("id-1")

        assertEquals("Replaced", result!!.title)
        dao.getAllNotes("uid-1").test {
            assertEquals(1, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun updateNote_changesSyncStatus() = runTest {
        val note = entity("id-1", syncStatus = SyncStatus.PENDING_CREATE)
        dao.insertNote(note)

        dao.updateNote(note.copy(syncStatus = SyncStatus.SYNCED.name))

        val result = dao.getNoteById("id-1")
        assertEquals(SyncStatus.SYNCED.name, result!!.syncStatus)
    }

    @Test
    fun deleteNote_removesFromDB() = runTest {
        val note = entity("id-1")
        dao.insertNote(note)

        dao.deleteNote(note)

        assertNull(dao.getNoteById("id-1"))
    }

    @Test
    fun deleteSyncedForUser_keepsPendingEntities() = runTest {
        dao.insertNote(entity("id-1", syncStatus = SyncStatus.SYNCED))
        dao.insertNote(entity("id-2", syncStatus = SyncStatus.PENDING_CREATE))

        dao.deleteSyncedForUser("uid-1")

        assertNull(dao.getNoteById("id-1"))
        assertNotNull(dao.getNoteById("id-2"))
    }

    @Test
    fun getPendingSync_returnsAllNonSyncedStatuses() = runTest {
        dao.insertNote(entity("id-1", syncStatus = SyncStatus.PENDING_CREATE))
        dao.insertNote(entity("id-2", syncStatus = SyncStatus.PENDING_UPDATE))
        dao.insertNote(entity("id-3", syncStatus = SyncStatus.PENDING_DELETE))
        dao.insertNote(entity("id-4", syncStatus = SyncStatus.SYNCED))

        val pending = dao.getPendingSync("uid-1")

        assertEquals(3, pending.size)
        assert(pending.none { it.syncStatus == SyncStatus.SYNCED.name })
    }

    @Test
    fun getNoteByServerId_returnsMatchingEntity() = runTest {
        dao.insertNote(entity("id-1", serverId = "srv-abc"))

        val result = dao.getNoteByServerId("srv-abc")

        assertNotNull(result)
        assertEquals("id-1", result!!.id)
    }

    @Test
    fun getNoteByServerId_noMatch_returnsNull() = runTest {
        val result = dao.getNoteByServerId("unknown-srv")

        assertNull(result)
    }
}
