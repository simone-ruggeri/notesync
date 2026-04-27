package com.notesync.data.remote

import com.notesync.data.remote.dto.AuthRequest
import com.notesync.data.remote.dto.AuthResponseDto
import com.notesync.data.remote.dto.CreateNoteRequest
import com.notesync.data.remote.dto.NoteDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    @POST("api/auth/register")
    suspend fun register(@Body request: AuthRequest): Response<AuthResponseDto>

    @POST("api/auth/login")
    suspend fun login(@Body request: AuthRequest): Response<AuthResponseDto>

    @GET("api/notes")
    suspend fun getNotes(): Response<List<NoteDto>>

    @POST("api/notes")
    suspend fun createNote(@Body request: CreateNoteRequest): Response<NoteDto>

    @PUT("api/notes/{id}")
    suspend fun updateNote(
        @Path("id") id: String,
        @Body request: CreateNoteRequest
    ): Response<NoteDto>

    @DELETE("api/notes/{id}")
    suspend fun deleteNote(@Path("id") id: String): Response<Unit>
}