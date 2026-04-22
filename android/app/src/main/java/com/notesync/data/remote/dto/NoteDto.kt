package com.notesync.data.remote.dto

import com.google.gson.annotations.SerializedName

// DTOs — Data Transfer Objects: oggetti usati per la comunicazione di rete
// Separati dai modelli di dominio per disaccoppiare la UI dai dettagli della rete
data class NoteDto(
    val id: String,
    val userId: String,
    val title: String,
    val content: String,
    @SerializedName("createdAt") val createdAt: Long,
    @SerializedName("updatedAt") val updatedAt: Long
)

data class CreateNoteRequest(
    val title: String,
    val content: String
)