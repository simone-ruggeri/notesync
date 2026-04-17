package com.notesync.repositories

import com.example.models.NoteRequest
import com.example.plugins.Notes
import com.example.plugins.Users
import com.example.repositories.NoteRepository
import com.example.repositories.UserRepository
import com.notesync.testutils.TestDatabase
import io.ktor.server.config.ApplicationConfig
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.util.UUID
import kotlin.test.*

class NoteRepositoryTest {
    private val noteRepo = NoteRepository()
    private val userRepo = UserRepository()
    private lateinit var testUserId: String

    // @BeforeTest viene eseguita PRIMA di ogni singolo test
    @BeforeTest
    fun setup() {
        TestDatabase.connect()

        // Crea le tabelle da zero
        transaction { SchemaUtils.create(Users, Notes) }
        // Crea un utente di test da usare come proprietario delle note
        // cost 4 = veloce per i test
        val hash = BCrypt.hashpw("password123", BCrypt.gensalt(4))
        testUserId = userRepo.create("test@example.com", hash).id
    }

    // @AfterTest viene eseguita DOPO ogni singolo test
    // Pulisce il database per non lasciare dati "sporchi" tra un test e l'altro
    @AfterTest
    fun teardown() {
        transaction { SchemaUtils.drop(Notes, Users) }
    }

    // ── Test di creazione ────────────────────────────────
    @Test
    fun `create restituisce una nota con ID generato`() {
        // Arrange: prepara i dati di input
        val request = NoteRequest(
            title = "Titolo test", content = "Contenuto test"
        )
        // Act: esegui l'operazione da testare
        val nota = noteRepo.create(testUserId, request)
        // Assert: verifica che il risultato sia quello atteso
        // assertNotNull: fallisce se l'ID è null
        assertNotNull(nota.id, "L'ID non deve essere null")
        // assertEquals: fallisce se i valori non sono uguali
        assertEquals("Titolo test", nota.title)
        assertEquals("Contenuto test", nota.content)
        assertEquals(testUserId, nota.userId)
    }

    @Test
    fun `create imposta createdAt e updatedAt uguali alla creazione`() {
        val nota = noteRepo.create(testUserId, NoteRequest("T", "C"))
        // Alla creazione i due timestamp devono coincidere
        assertEquals(nota.createdAt, nota.updatedAt)
    }

    // ── Test di lettura ──────────────────────────────────
    @Test
    fun `getAllByUser restituisce solo le note dell'utente specificato`() {
        // Crea un secondo utente
        val altroHash = BCrypt.hashpw("pass", BCrypt.gensalt(4))
        val altroUserId = userRepo.create("altro@example.com", altroHash).id
        // Crea note per entrambi gli utenti
        noteRepo.create(testUserId, NoteRequest("Nota di test", ""))
        noteRepo.create(testUserId, NoteRequest("Nota 2 di test", ""))
        noteRepo.create(altroUserId, NoteRequest("Nota di altro", ""))
        // testUserId deve vedere solo le sue 2 note, non quella dell'altro
        val noteUtente = noteRepo.getAllByUser(testUserId)
        assertEquals(2, noteUtente.size, "Deve restituire esattamente 2 note")
        assertTrue(noteUtente.all { it.userId == testUserId })
    }

    @Test
    fun `getAllByUser restituisce lista vuota se non ci sono note`() {
        val note = noteRepo.getAllByUser(testUserId)
        assertTrue(note.isEmpty(), "La lista deve essere vuota")
    }

    // ── Test di aggiornamento ────────────────────────────
    @Test
    fun `update modifica titolo e contenuto della nota`() {
        val nota = noteRepo.create(
            testUserId, NoteRequest( "Originale", "Contenuto originale ")
        )
        val aggiornata = noteRepo.update(
            id = nota.id,
            userId = testUserId,
            request = NoteRequest("Aggiornato", "Contenuto aggiornato")
        )
        assertNotNull(aggiornata, "La nota aggiornata non deve essere null")
        assertEquals("Aggiornato", aggiornata.title)
        assertEquals("Contenuto aggiornato", aggiornata.content)
        // updatedAt deve essere >= createdAt dopo l'aggiornamento
        assertTrue(aggiornata.updatedAt >= aggiornata.createdAt)
    }

    @Test
    fun `update restituisce null se la nota non appartiene all'utente`() {
        // Crea una nota come testUserId
        val nota = noteRepo.create(testUserId, NoteRequest("Privata", ""))
        val altroUserId = UUID.randomUUID().toString()

        val risultato = noteRepo.update(
            id = nota.id,
            userId = altroUserId,
            request = NoteRequest("Tentativo", "")
        )
        // Deve tornare null: l'operazione non deve avere effetto
        assertNull(risultato, "Non deve essere possibile modificare note altrui")
    }

    // ── Test di cancellazione ────────────────────────────
    @Test
    fun `delete rimuove la nota e restituisce true`() {
        val nota = noteRepo.create(testUserId, NoteRequest("Da cancellare", ""))
        val cancellata = noteRepo.delete(nota.id, testUserId)
        assertTrue(cancellata, "delete deve restituire true se la nota esisteva")
        // Verifica che la nota non esista più
        val note = noteRepo.getAllByUser(testUserId)
        assertTrue(
            note.isEmpty(), "La lista deve essere vuota dopo la cancellazione "
        )
    }

    @Test
    fun `delete restituisce false se la nota non esiste`() {
        val fakeId = UUID.randomUUID().toString()

        val risultato = noteRepo.delete(fakeId, testUserId)

        assertFalse(risultato, "delete deve restituire false se la nota non esiste ")
    }

    @Test
    fun `delete non cancella note di altri utenti`() {
        val altroHash = BCrypt.hashpw("pass", BCrypt.gensalt(4))
        val altroUserId = userRepo.create("altro2@example.com", altroHash).id
        val nota = noteRepo.create(altroUserId, NoteRequest("Nota privata", ""))
        // testUserId prova a cancellare la nota di altroUserId
        val risultato = noteRepo.delete(nota.id, testUserId)
        assertFalse(risultato)
        // La nota deve esistere ancora
        assertEquals(1, noteRepo.getAllByUser(altroUserId).size)
    }
}