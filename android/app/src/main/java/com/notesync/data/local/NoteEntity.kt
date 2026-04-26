package com.notesync.data.local

import androidx.room.*

// @Entity definisce una tabella in SQLite
// tableName è il nome fisico della tabella
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey
    val id: String,        // UUID locale (generato dall'app, non dal server)
    val serverId: String?, // ID assegnato dal backend (null fino a sincronizzazione)
    val userId: String,    // proprietario della nota, usato per isolare i dati tra utenti
    val title: String,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long,

    // SyncStatus tiene traccia dello stato di sincronizzazione
    // Valori possibili: PENDING_CREATE, PENDING_UPDATE, PENDING_DELETE, SYNCED
    val syncStatus: String = SyncStatus.PENDING_CREATE.name
)
// Enum che rappresenta i possibili stati di sincronizzazione
enum class SyncStatus {
    PENDING_CREATE, // nota creata localmente, non ancora inviata al server
    PENDING_UPDATE, // nota modificata localmente, modifica non sincronizzata
    PENDING_DELETE, // nota eliminata localmente, eliminazione non sincronizzata
    SYNCED          // nota in sync con il server
}