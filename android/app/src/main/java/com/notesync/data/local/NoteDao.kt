package com.notesync.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// @Dao = Data Access Object
// Interfaccia con le operazioni che vogliamo fare sul database
@Dao
interface NoteDao {
    // Flow<List<NoteEntity>>: emette automaticamente una nuova lista
    // ogni volta che i dati nel database cambiano.
    // Questo è il "reactive programming": la UI si aggiorna da sola.
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: String): NoteEntity?

    // @Insert con OnConflictStrategy.REPLACE:
    // se esiste già una nota con lo stesso ID, la sovrascrive.
    // Utile durante la sincronizzazione.
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

    // Recupera tutte le note che devono essere sincronizzate
    @Query("SELECT * FROM notes WHERE syncStatus != 'SYNCED'")
    suspend fun getPendingSync(): List<NoteEntity>
}