package com.example.routes

import org.mindrot.jbcrypt.BCrypt
import com.example.repositories.UserRepository
import com.example.security.JwtConfig
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable


@Serializable
data class RegisterRequest(val email: String, val password: String)

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class AuthResponse(val token: String, val userId: String)

fun Route.authRoutes(userRepo: UserRepository) {
    route("/api/auth") {
        // Legge il secret una volta sola per tutte le route del blocco
        val secret = application.environment.config.property("jwt.secret").getString()

        // POST /api/auth/register
        post("/register") {
            val request = call.receive<RegisterRequest>()

            if (!request.email.contains("@")) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Email non valida"))
                return@post
            }

            if (userRepo.findByEmail(request.email) != null) {
                call.respond(HttpStatusCode.Conflict, mapOf("error" to "Email già registrata"))
                return@post
            }

            val hashedPassword = BCrypt.hashpw(request.password, BCrypt.gensalt(10))
            val user = userRepo.create(request.email, hashedPassword)

            val token = JwtConfig.generateToken(user.id, secret) // ← passa secret
            call.respond(HttpStatusCode.Created, AuthResponse(token, user.id))
        }

        // POST /api/auth/login
        post("/login") {
            val request = call.receive<LoginRequest>()
            val user = userRepo.findByEmail(request.email)

            if (user == null || !BCrypt.checkpw(request.password, user.passwordHash)) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Credenziali non valide"))
                return@post
            }

            val token = JwtConfig.generateToken(user.id, secret) // ← passa secret
            call.respond(HttpStatusCode.OK, AuthResponse(token, user.id))
        }
    }
}