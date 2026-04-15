package com.example.routes

import com.example.models.NoteRequest
import com.example.repositories.NoteRepository
import io.ktor.http.HttpStatusCode
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

fun Route.noteRoutes(noteRepo: NoteRepository) {
    // authenticate("auth-jwt") significa che tutte le route interne
    // richiedono un JWT token valido nell'header Authorization
    authenticate("auth-jwt") {
        route("/api/notes") {
            // GET /api/notes — ottieni tutte le note dell'utente loggato
            get {
                // Il JWT contiene il userId come "subject"
                val userId = call.principal<JWTPrincipal>()!!
                    .payload.subject
                val notes = noteRepo.getAllByUser(userId)
                call.respond(HttpStatusCode.OK, notes)
            }

            // POST /api/notes — crea una nuova nota
            post {
                val userId = call.principal<JWTPrincipal>()!!.payload.subject
                val request = call.receive<NoteRequest>() // deserializza JSON body

                // Validazione semplice
                if (request.title.isBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Il titolo non può essere vuoto"))
                    return@post
                }
                val note = noteRepo.create(userId, request)
                call.respond(HttpStatusCode.Created, note) // 201 Created
            }

            // PUT /api/notes/{id} — aggiorna una nota
            put("/{id}") {
                val userId = call.principal<JWTPrincipal>()!!.payload.subject
                val noteId = call.parameters["id"]
                    ?: return@put call.respond(HttpStatusCode.BadRequest)
                val request = call.receive<NoteRequest>()
                val updated = noteRepo.update(noteId, userId, request)
                if (updated == null) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        mapOf("error" to "Nota non trovata"))
                } else {
                    call.respond(HttpStatusCode.OK, updated)
                }
            }

            // DELETE /api/notes/{id}
            delete("/{id}") {
                val userId = call.principal<JWTPrincipal>()!!.payload.subject
                val noteId = call.parameters["id"]
                    ?: return@delete call.respond(HttpStatusCode.BadRequest)
                val deleted = noteRepo.delete(noteId, userId)
                if (deleted) {
                    call.respond(HttpStatusCode.NoContent) // 204 = successo senza body
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
        }
    }
}