package com.notesync.routes

import com.notesync.testutils.baseTestApp
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.*


class AuthRoutesTest {
    // testApplication è una funzione di Ktor che avvia il server in memoria.
    // Il blocco application { module() } carica l'applicazione completa.
    // Il client HTTP è già configurato per comunicare con questo server virtuale.
    @Test
    fun `register con dati validi restituisce 201 e un token`() = testApplication {
        baseTestApp()
        // Invia una POST a /api/auth/register
        val response = client.post("/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{ "email": "test@test.com", "password": "pass123" }""")
        }
        // Verifica lo status code
        assertEquals(HttpStatusCode.Created, response.status)
        // Parsa il body JSON della risposta
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        // Verifica che il campo "token" esista e non sia vuoto
        assertNotNull(body["token"], "La risposta deve contenere il token")
        assertTrue(body["token"]!!.jsonPrimitive.content.isNotEmpty())
        // Verifica che il campo "userId" esista
        assertNotNull(body["userId"], "La risposta deve contenere userId")
    }

    @Test
    fun `register con email già in uso restituisce 409`() = testApplication {
        baseTestApp()
        val body = """{ "email": "doppio@test.com", "password": "pass123" }"""
        // Prima registrazione: deve andare a buon fine
        client.post("/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        // Seconda registrazione con la stessa email: deve fallire
        val response = client.post("/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        assertEquals(HttpStatusCode.Conflict, response.status)
    }

    @Test
    fun `register con email senza @ restituisce 400`() = testApplication {
        baseTestApp()
        val response = client.post("/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{ "email": "emailnonvalida", "password": "pass123" }""")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `login con credenziali corrette restituisce 200 e un token`() = testApplication {
        baseTestApp()
        // Prima registra l'utente
        client.post("/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{ "email": "login@test.com", "password": "pass123" }""")
        }
        // Poi fai login
        val response = client.post("/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{ "email": "login@test.com", "password": "pass123" }""")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertNotNull(body["token"])
    }

    @Test
    fun `login con password sbagliata restituisce 401`() = testApplication {
        baseTestApp()
        client.post("/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{ "email": "user@test.com", "password": "pass123" }""")
        }
        val response = client.post("/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{ "email": "user@test.com", "password": "SBAGLIATA" }""")
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `login con email inesistente restituisce 401`() = testApplication {
        baseTestApp()
        val response = client.post("/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{ "email": "fantasma@test.com", "password": "pass" }""")
        }
        // 401, non 404: non riveliamo se l'email esiste o meno
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}