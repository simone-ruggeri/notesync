package com.example.repositories

import com.example.Users
import com.example.models.User
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class UserRepository {
    // Cerca un utente per email — usato durante il login
    // Ritorna null se l'email non esiste (non lanciamo eccezioni per casi normali)
    fun findByEmail(email: String): User? = transaction {
        Users.selectAll().where { Users.email eq email }
            .firstOrNull()
            ?.let { rowToUser(it) }
    }

    // Cerca un utente per ID — utile per recuperare dati del profilo
    fun findById(id: String): User? = transaction {
        Users.selectAll().where { Users.id eq UUID.fromString(id) }
            .firstOrNull()
            ?.let { rowToUser(it) }
    }

    // Crea un nuovo utente nel database.
    // passwordHash è già l'hash BCrypt — non la password originale.
    // Ritorna l'oggetto User appena creato.
    fun create(email: String, passwordHash: String): User = transaction {
        val newId = UUID.randomUUID()
        val now = System.currentTimeMillis()
        Users.insert {
            it[id] = newId
            it[Users.email] = email
            it[Users.password] = passwordHash
            it[createdAt] = now
        }
        User(
            id = newId.toString(),
            email = email,
            passwordHash = passwordHash,
            createdAt = now
        )
    }

    // Funzione helper privata: converte una riga del DB in un oggetto User.
    private fun rowToUser(row: ResultRow) = User(
        id = row[Users.id].toString(),
        email = row[Users.email],
        passwordHash = row[Users.password],
        createdAt = row[Users.createdAt]
    )
}