package com.example.routes

import org.mindrot.jbcrypt.BCrypt
import com.example.repositories.UserRepository
import com.example.security.JwtConfig
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
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
        // POST /api/auth/register
        post("/register") {
            val request = call.receive<RegisterRequest>()

            // Validazione email
            if (!request.email.contains("@")) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Email non valida")
                )
                return@post
            }

            // Controlla se l'email è già in uso
            if (userRepo.findByEmail(request.email) != null) {
                call.respond(
                    HttpStatusCode.Conflict,
                    mapOf("error" to "Email già registrata")
                )
                return@post
            }

            // BCrypt hash della password — MAI salvare password in chiaro
            // Il secondo parametro (10) è il "cost factor":
            // più alto = più sicuro ma più lento
            val hashedPassword = BCrypt.hashpw(
                request.password,
                BCrypt.gensalt(10)
            )
            val user = userRepo.create(request.email, hashedPassword)
            val token = JwtConfig.generateToken(user.id)
            call.respond(HttpStatusCode.Created, AuthResponse(token, user.id))
        }

        // POST /api/auth/login
        post("/login") {
            val request = call.receive<LoginRequest>()
            val user = userRepo.findByEmail(request.email)

            // BCrypt.checkpw confronta la password con l'hash salvato
            // Questo previene attacchi di timing (a differenza di ==)
            if (user == null || !BCrypt.checkpw(request.password, user.passwordHash)) {
                // Nota: ritorniamo lo STESSO messaggio per email sbagliata e password sbagliata
                // Questo previene l'enumerazione di utenti esistenti
                call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to "Credenziali non valide")
                )
                return@post
            }
            val token = JwtConfig.generateToken(user.id)
            call.respond(HttpStatusCode.OK, AuthResponse(token, user.id))
        }
    }
}