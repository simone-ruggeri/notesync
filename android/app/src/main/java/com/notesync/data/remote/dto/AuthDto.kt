package com.notesync.data.remote.dto

data class AuthRequest(
    val email: String,
    val password: String
)

data class AuthResponseDto(
    val token: String,
    val userId: String
)