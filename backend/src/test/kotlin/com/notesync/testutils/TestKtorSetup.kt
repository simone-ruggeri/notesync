package com.notesync.testutils

import com.example.module
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.ApplicationTestBuilder

fun ApplicationTestBuilder.baseTestApp() {
    environment {
        config = MapApplicationConfig(
            "postgres.url" to "jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
            "postgres.user" to "test",
            "postgres.password" to "",

            "jwt.secret" to "test-secret-chiave-per-i-test-lunga-abbastanza",
            "jwt.domain" to "https://jwt-provider-domain/",
            "jwt.audience" to "jwt-audience",
            "jwt.realm" to "ktor sample app"
        )
    }

    application {
        module()
    }
}