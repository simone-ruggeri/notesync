package com.example.models

import kotlinx.serialization.Serializable

// Rappresenta un utente come viene letto dal database.
// passwordHash NON viene mai esposto nelle risposte HTTP:
// serve solo internamente per la verifica del login.
@Serializable
data class User(
    val id: String,
    val email: String,
    val passwordHash: String, // hash BCrypt, mai la password in chiaro
    val createdAt: Long
)

// Oggetto usato SOLO nelle risposte HTTP: non include passwordHash.
// È buona pratica avere oggetti distinti per rappresentazione interna
// e per ciò che viene inviato al client.
@Serializable
data class UserResponse(
    val id: String,
    val email: String,
    val createdAt: Long
)
