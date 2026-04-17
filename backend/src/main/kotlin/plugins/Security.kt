package com.example.plugins

import com.example.security.JwtConfig
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Application.configureSecurity() {
    val secret = environment.config.property("jwt.secret").getString()

    authentication {
        jwt("auth-jwt") {
            verifier(JwtConfig.getVerifier(secret))  // passa il secret
            validate { credential ->
                if (credential.payload.subject != null) JWTPrincipal(credential.payload)
                else null
            }
        }
    }
}
