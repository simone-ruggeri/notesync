package com.notesync.repositories

import com.example.plugins.Users
import com.example.repositories.UserRepository
import com.notesync.testutils.TestDatabase
import io.ktor.server.config.ApplicationConfig
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import kotlin.test.*

class UserRepositoryTest {
    private val repo = UserRepository()

    @BeforeTest
    fun setup() {
        TestDatabase.connect()

        transaction { SchemaUtils.create(Users) }
    }

    @AfterTest
    fun teardown() {
        transaction { SchemaUtils.drop(Users) }
    }

    @Test
    fun `create restituisce utente con ID e email corretti`() {
        val hash = BCrypt.hashpw("pass", BCrypt.gensalt(4))
        val user = repo.create("mario@example.com", hash)
        assertNotNull(user.id)
        assertEquals("mario@example.com", user.email)
        // Verifica che l'hash salvato sia quello passato, non la password in chiaro
        assertEquals(hash, user.passwordHash)
    }

    @Test
    fun `findByEmail restituisce l'utente se esiste`() {
        val hash = BCrypt.hashpw("pass", BCrypt.gensalt(4))
        repo.create("luigi@example.com", hash)
        val trovato = repo.findByEmail("luigi@example.com")
        assertNotNull(trovato, "L'utente deve essere presente")
        assertEquals("luigi@example.com", trovato.email)
    }

    @Test
    fun `findByEmail restituisce null se l'email non esiste`() {
        val risultato = repo.findByEmail("nonesiste@example.com")
        assertNull(risultato, "Deve restituire null per email inesistente")
    }

    @Test
    fun `create due utenti con email diverse funziona correttamente`() {
        val hash = BCrypt.hashpw("pass", BCrypt.gensalt(4))
        val user1 = repo.create("uno@example.com", hash)
        val user2 = repo.create("due@example.com", hash)
        // Gli ID devono essere diversi
        assertNotEquals(user1.id, user2.id)
    }

    @Test
    fun `create con email duplicata lancia eccezione`() {
        val hash = BCrypt.hashpw("pass", BCrypt.gensalt(4))
        repo.create("doppio@example.com", hash)
        // assertFails verifica che il blocco lanci un'eccezione
        // Il vincolo UNIQUE sulla colonna email deve impedire duplicati
        assertFails {
            repo.create("doppio@example.com", hash)
        }
    }
}