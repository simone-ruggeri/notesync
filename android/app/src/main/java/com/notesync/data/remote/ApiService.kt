package com.notesync.data.remote

import com.notesync.data.remote.dto.AuthRequest
import com.notesync.data.remote.dto.AuthResponseDto
import com.notesync.data.remote.dto.CreateNoteRequest
import com.notesync.data.remote.dto.NoteDto
import retrofit2.Response
import retrofit2.http.*

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