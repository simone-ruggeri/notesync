package com.example.plugins

import com.example.repositories.NoteRepository
import com.example.repositories.UserRepository
import com.example.routes.authRoutes
import com.example.routes.noteRoutes
import io.ktor.server.application.*
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    val userRepo = UserRepository()
    val noteRepo = NoteRepository()
    // Il secret è letto una sola volta qui e passato esplicitamente alle route che lo usano.
    // Security.kt lo legge separatamente per configurare il JWT verifier.
    val jwtSecret = environment.config.property("jwt.secret").getString()

    routing {
        authRoutes(userRepo, jwtSecret)
        noteRoutes(noteRepo)
    }
}
