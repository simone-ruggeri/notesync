package com.example.routes

import com.example.models.NoteRequest
import com.example.repositories.NoteRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import java.util.UUID

// Estrae l'userId dal JWT già validato da authenticate("auth-jwt").
// L'operatore !! è sicuro qui: il blocco authenticate garantisce che
// il principal esista prima di eseguire qualsiasi handler interno.
private fun ApplicationCall.jwtUserId(): String =
    principal<JWTPrincipal>()!!.payload.subject

// Restituisce false se la stringa non è un UUID valido.
// Usata per rispondere 400 invece di 500 su parametri URL malformati.
private fun String.isValidUUID(): Boolean = try {
    UUID.fromString(this); true
} catch (e: IllegalArgumentException) { false }

fun Route.noteRoutes(noteRepo: NoteRepository) {
    // authenticate("auth-jwt") significa che tutte le route interne
    // richiedono un JWT token valido nell'header Authorization
    authenticate("auth-jwt") {
        route("/api/notes") {
            // GET /api/notes — ottieni tutte le note dell'utente loggato
            get {
                val notes = noteRepo.getAllByUser(call.jwtUserId())
                call.respond(HttpStatusCode.OK, notes)
            }

            // POST /api/notes — crea una nuova nota
            post {
                val request = call.receive<NoteRequest>() // deserializza JSON body
                if (request.title.isBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Il titolo non può essere vuoto"))
                    return@post
                }
                val note = noteRepo.create(call.jwtUserId(), request)
                call.respond(HttpStatusCode.Created, note) // 201 Created
            }

            // PUT /api/notes/{id} — aggiorna una nota
            put("/{id}") {
                val noteId = call.parameters["id"]
                    ?: return@put call.respond(HttpStatusCode.BadRequest)
                if (!noteId.isValidUUID())
                    return@put call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID non valido"))
                val request = call.receive<NoteRequest>()
                val updated = noteRepo.update(noteId, call.jwtUserId(), request)
                if (updated == null) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Nota non trovata"))
                } else {
                    call.respond(HttpStatusCode.OK, updated)
                }
            }

            // DELETE /api/notes/{id}
            delete("/{id}") {
                val noteId = call.parameters["id"]
                    ?: return@delete call.respond(HttpStatusCode.BadRequest)
                if (!noteId.isValidUUID())
                    return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID non valido"))
                val deleted = noteRepo.delete(noteId, call.jwtUserId())
                if (deleted) {
                    call.respond(HttpStatusCode.NoContent) // 204 = successo senza body
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
        }
    }
}