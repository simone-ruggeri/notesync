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

    routing {
        authRoutes(userRepo)
        noteRoutes(noteRepo)
    }
}
