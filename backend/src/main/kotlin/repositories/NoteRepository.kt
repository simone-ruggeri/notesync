package com.example.repositories

import com.example.plugins.Notes
import com.example.models.Note
import com.example.models.NoteRequest
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class NoteRepository {

    fun getAllByUser(userId: String): List<Note> = transaction {
        Notes.selectAll()
            .where { Notes.userId eq UUID.fromString(userId) }
            .map { rowToNote(it) }
    }

    fun create(userId: String, request: NoteRequest): Note = transaction {
        val newId = UUID.randomUUID()
        val now = System.currentTimeMillis()
        Notes.insert {
            it[id] = newId
            it[Notes.userId] = UUID.fromString(userId)
            it[title] = request.title
            it[content] = request.content
            it[createdAt] = now
            it[updatedAt] = now
        }
        Note(
            id = newId.toString(),
            userId = userId,
            title = request.title,
            content = request.content,
            createdAt = now,
            updatedAt = now
        )
    }

    fun update(id: String, userId: String, request: NoteRequest): Note? = transaction {
        val updated = Notes.update(
            where = {
                (Notes.id eq UUID.fromString(id)) and
                        (Notes.userId eq UUID.fromString(userId))
            }
        ) {
            it[title] = request.title
            it[content] = request.content
            it[updatedAt] = System.currentTimeMillis()
        }
        if (updated == 0) null
        else getById(id, userId)
    }

    fun delete(id: String, userId: String): Boolean = transaction {
        Notes.deleteWhere {
            (Notes.id eq UUID.fromString(id)) and
                    (Notes.userId eq UUID.fromString(userId))
        } > 0
    }

    private fun getById(id: String, userId: String): Note? = transaction {
        Notes.selectAll()
            .where {
                (Notes.id eq UUID.fromString(id)) and
                        (Notes.userId eq UUID.fromString(userId))
            }
            .firstOrNull()?.let { rowToNote(it) }
    }

    private fun rowToNote(row: ResultRow) = Note(
        id        = row[Notes.id].toString(),
        userId    = row[Notes.userId].toString(),
        title     = row[Notes.title],
        content   = row[Notes.content],
        createdAt = row[Notes.createdAt],
        updatedAt = row[Notes.updatedAt]
    )
}