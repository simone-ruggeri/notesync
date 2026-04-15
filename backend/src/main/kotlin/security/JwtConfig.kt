package com.example.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date

object JwtConfig {
    // Il secret è la chiave privata con cui firmiamo i token
    // NON deve essere mai esposto o committato in repository pubblici
    private val secret = System.getenv("JWT_SECRET")
        ?: error("JWT_SECRET environment variable not set")
    private val algorithm = Algorithm.HMAC256(secret)

    // Generazione del token: chiamata dopo il login
    fun generateToken(userId: String): String {
        return JWT.create()
            .withIssuer("notesync")       // chi ha emesso il token
            .withAudience("notesync-app") // a chi è destinato
            .withSubject(userId)          // dati utente nel payload
            .withExpiresAt(Date(System.currentTimeMillis() + 86_400_000L)) // 24 ore
            .sign(algorithm)
    }

    // Validazione: usata da Ktor per verificare ogni richiesta protetta
    fun getVerifier() = JWT.require(algorithm)
        .withIssuer("notesync")
        .withAudience("notesync-app")
        .build()
}