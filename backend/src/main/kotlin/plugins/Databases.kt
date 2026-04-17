package com.example.plugins

import io.ktor.server.application.Application
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SchemaUtils
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.util.UUID

// Tabella Users
object Users : Table("users") {
    val id = uuid("id").clientDefault { UUID.randomUUID() }

    val email = varchar("email", 255).uniqueIndex()
    val password = varchar("password_hash", 255)

    val createdAt = long("created_at")
        .clientDefault { System.currentTimeMillis() }

    override val primaryKey = PrimaryKey(id)
}

// Tabella Notes
object Notes : Table("notes") {
    val id = uuid("id").clientDefault { UUID.randomUUID() }

    val userId = uuid("user_id").references(
        Users.id,
        onDelete = ReferenceOption.CASCADE
    )

    val title = varchar("title", 500)
    val content = text("content")

    val createdAt = long("created_at")
        .clientDefault { System.currentTimeMillis() }

    val updatedAt = long("updated_at")
        .clientDefault { System.currentTimeMillis() }

    override val primaryKey = PrimaryKey(id)
}

// Funzione di configurazione
fun Application.configureDatabases() {
    // Legge prima dalla configurazione Ktor (usata nei test),
    // poi dalle variabili d'ambiente (usata in produzione/Docker)
    val dbUrl = environment.config.propertyOrNull("postgres.url")?.getString()
        ?: System.getenv("DB_URL")
        ?: "jdbc:postgresql://localhost:5432/notesync"

    val dbUser = environment.config.propertyOrNull("postgres.user")?.getString()
        ?: System.getenv("DB_USER") ?: "notesync_user"

    val dbPass =
        environment.config.propertyOrNull("postgres.password")?.getString()
            ?: System.getenv("DB_PASS") ?: "notesync_pass"

    val config = HikariConfig().apply {
        jdbcUrl = dbUrl
        username = dbUser
        password = dbPass
        driverClassName = "org.postgresql.Driver"
        maximumPoolSize = 10
    }

    val dataSource = HikariDataSource(config)

    Database.connect(dataSource)

    transaction {
        SchemaUtils.create(Users, Notes)
    }
}