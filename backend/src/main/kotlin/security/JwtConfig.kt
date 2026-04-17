package com.example.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date

object JwtConfig {

    // Genera il token usando il secret passato dal chiamante (da application.yaml)
    fun generateToken(userId: String, secret: String): String {
        val algorithm = Algorithm.HMAC256(secret)
        return JWT.create()
            .withIssuer("notesync")
            .withAudience("notesync-app")
            .withSubject(userId)
            .withExpiresAt(Date(System.currentTimeMillis() + 86_400_000L)) // 24 ore
            .sign(algorithm)
    }

    // Verifier usando il secret passato dal chiamante
    fun getVerifier(secret: String) = JWT
        .require(Algorithm.HMAC256(secret))
        .withIssuer("notesync")
        .withAudience("notesync-app")
        .build()
}