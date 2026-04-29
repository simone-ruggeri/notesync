package com.notesync

import com.notesync.data.remote.ApiService
import com.notesync.data.remote.dto.AuthRequest
import com.notesync.data.remote.dto.AuthResponseDto
import com.notesync.data.remote.dto.CreateNoteRequest
import com.notesync.data.remote.dto.NoteDto
import retrofit2.Response

class StubApiService : ApiService {
    var loginResult: Response<AuthResponseDto> =
        Response.success(AuthResponseDto("test-token", "uid-test"))
    var registerResult: Response<AuthResponseDto> =
        Response.success(AuthResponseDto("test-token", "uid-test"))

    override suspend fun register(request: AuthRequest) = registerResult
    override suspend fun login(request: AuthRequest) = loginResult
    override suspend fun getNotes() = Response.success<List<NoteDto>>(emptyList())
    override suspend fun createNote(request: CreateNoteRequest) =
        Response.success(NoteDto("srv-1", "uid-test", request.title, request.content, 0L, System.currentTimeMillis()))
    override suspend fun updateNote(id: String, request: CreateNoteRequest) =
        Response.success(NoteDto(id, "uid-test", request.title, request.content, 0L, System.currentTimeMillis()))
    override suspend fun deleteNote(id: String) = Response.success(Unit)
}
