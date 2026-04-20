package com.notesync.data.remote

import retrofit2.Response
import retrofit2.http.*

// DTOs — Data Transfer Objects: oggetti usati per la comunicazione di rete
// Separati dai modelli di dominio per disaccoppiare la UI dai dettagli della rete
data class NoteDto(
    val id: String, val title: String, val content: String,
    val createdAt: Long, val updatedAt: Long
)

data class CreateNoteRequest(val title: String, val content: String)
data class AuthRequest(val email: String, val password: String)
data class AuthResponseDto(val token: String, val userId: String)

interface ApiService {
    @POST("api/auth/register")
    suspend fun register(@Body request: AuthRequest): Response<AuthResponseDto>

    @POST("api/auth/login")
    suspend fun login(@Body request: AuthRequest): Response<AuthResponseDto>

    // @Header aggiunge l'header alla singola chiamata
    // Passiamo "Bearer $token" dove token è il JWT salvato
    @GET("api/notes")
    suspend fun getNotes(@Header("Authorization") token: String):
            Response<List<NoteDto>>

    @POST("api/notes")
    suspend fun createNote(
        @Header("Authorization") token: String,
        @Body request: CreateNoteRequest
    ): Response<NoteDto>

    @PUT("api/notes/{id}")
    suspend fun updateNote(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: CreateNoteRequest
    ): Response<NoteDto>

    @DELETE("api/notes/{id}")
    suspend fun deleteNote(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Unit>
}