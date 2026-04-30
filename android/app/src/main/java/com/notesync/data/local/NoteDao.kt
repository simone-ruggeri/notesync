package com.notesync.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE userId = :userId AND syncStatus != 'PENDING_DELETE' ORDER BY updatedAt DESC")
    fun getAllNotes(userId: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: String): NoteEntity?

    @Query("SELECT * FROM notes WHERE serverId = :serverId LIMIT 1")
    suspend fun getNoteByServerId(serverId: String): NoteEntity?

    @Query("SELECT * FROM notes WHERE userId = :userId AND title = :title AND content = :content AND syncStatus = 'PENDING_CREATE' LIMIT 1")
    suspend fun getPendingCreateByContent(userId: String, title: String, content: String): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notes: List<NoteEntity>)

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("DELETE FROM notes")
    suspend fun deleteAll()

    @Query("DELETE FROM notes WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)

    @Query("DELETE FROM notes WHERE userId = :userId AND syncStatus = 'SYNCED'")
    suspend fun deleteSyncedForUser(userId: String)

    // Elimina le note SYNCED il cui serverId non è più presente nella risposta del server.
    // Usata dopo ogni refresh per rilevare note cancellate da altri client (es. web).
    // Le note PENDING_* non vengono toccate: sono operazioni offline non ancora inviate.
    @Query("DELETE FROM notes WHERE userId = :userId AND syncStatus = 'SYNCED' AND serverId NOT IN (:serverIds)")
    suspend fun deleteSyncedNotesNotInServerIds(userId: String, serverIds: List<String>)

    @Query("SELECT * FROM notes WHERE userId = :userId AND syncStatus != 'SYNCED'")
    suspend fun getPendingSync(userId: String): List<NoteEntity>
}
