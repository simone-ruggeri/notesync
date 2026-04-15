package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class Note(
    val id: String,
    val userId: String,
    val title: String,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long
)

// Oggetto ricevuto quando l'app crea/aggiorna una nota
// Non include id, userId, createdAt (li genera il server)
@Serializable
data class NoteRequest(
    val title: String,
    val content: String
)