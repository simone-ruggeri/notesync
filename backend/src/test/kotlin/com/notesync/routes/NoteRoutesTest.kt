package com.notesync.routes

import com.notesync.testutils.baseTestApp
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.*
import kotlin.test.*

class NoteRoutesTest {
    // ── HELPERS ─────────────────────────────────────────
    private suspend fun registerAndGetToken(
        client: HttpClient,
        email: String = "test@test.com"
    ): String {
        val response = client.post("/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{ "email": "$email", "password": "pass123" }""")
        }
        return Json.parseToJsonElement(response.bodyAsText())
            .jsonObject["token"]!!
            .jsonPrimitive.content
    }

    private suspend fun createNote(
        client: HttpClient,
        token: String,
        title: String = "Nota di test",
        content: String = "Contenuto"
    ): String {
        val response = client.post("/api/notes") {
            contentType(ContentType.Application.Json)
            bearerAuth(token)
            setBody("""{ "title": "$title", "content": "$content" }""")
        }

        return Json.parseToJsonElement(response.bodyAsText())
            .jsonObject["id"]!!
            .jsonPrimitive.content
    }

    // ── TESTS ───────────────────────────────────────────

    @Test
    fun `GET notes senza token restituisce 401`() = testApplication {
        baseTestApp()
        val response = client.get("/api/notes")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `GET notes con token valido restituisce lista vuota inizialmente`() =
        testApplication {
            baseTestApp()

            val token = registerAndGetToken(client)

            val response = client.get("/api/notes") {
                bearerAuth(token)
            }

            assertEquals(HttpStatusCode.OK, response.status)
            assertTrue(Json.parseToJsonElement(response.bodyAsText()).jsonArray.isEmpty())
        }

    @Test
    fun `POST notes crea una nota e la restituisce con ID`() = testApplication {
        baseTestApp()

        val token = registerAndGetToken(client)

        val response = client.post("/api/notes") {
            contentType(ContentType.Application.Json)
            bearerAuth(token)
            setBody("""{ "title": "Titolo", "content": "Contenuto" }""")
        }

        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject

        assertEquals(HttpStatusCode.Created, response.status)
        assertNotNull(body["id"])
        assertEquals("Titolo", body["title"]!!.jsonPrimitive.content)
    }

    @Test
    fun `POST notes con titolo vuoto restituisce 400`() = testApplication {
        baseTestApp()

        val token = registerAndGetToken(client)

        val response = client.post("/api/notes") {
            contentType(ContentType.Application.Json)
            bearerAuth(token)
            setBody("""{ "title": "", "content": "Contenuto" }""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `PUT notes aggiorna titolo e contenuto`() = testApplication {
        baseTestApp()

        val token = registerAndGetToken(client)
        val noteId = createNote(client, token)

        val response = client.put("/api/notes/$noteId") {
            contentType(ContentType.Application.Json)
            bearerAuth(token)
            setBody("""{ "title": "Nuovo titolo", "content": "Nuovo contenuto" }""")
        }

        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Nuovo titolo", body["title"]!!.jsonPrimitive.content)
    }
}