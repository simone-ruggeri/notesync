package com.notesync.domain.model

import com.notesync.data.local.NoteEntity
import com.notesync.data.local.SyncStatus

// Modello di dominio: la UI lavora esclusivamente con questo tipo.
data class Note(
    val id: String,
    val title: String,
    val content: String,
    val updatedAt: Long,
    val syncStatus: SyncStatus,
    // Campo calcolato: evita di ripetere il confronto in ogni Composable
    val isSynced: Boolean = syncStatus == SyncStatus.SYNCED
)

// Extension function: converte NoteEntity (DB) -> Note (dominio).
fun NoteEntity.toDomain(): Note = Note(
    id = id,
    title = title,
    content = content,
    updatedAt = updatedAt,
    syncStatus = SyncStatus.valueOf(syncStatus)
)