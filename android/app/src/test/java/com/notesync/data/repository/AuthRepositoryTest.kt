package com.notesync.data.repository

import com.notesync.data.remote.ApiService
import com.notesync.data.remote.dto.AuthResponseDto
import com.notesync.util.MainDispatcherRule
import com.notesync.util.TokenManager
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import retrofit2.Response

class AuthRepositoryTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val apiService = mock<ApiService>()
    private val tokenManager = mock<TokenManager>()
    private lateinit var repository: AuthRepository

    @Before
    fun setup() {
        repository = AuthRepository(apiService, tokenManager)
    }

    @Test
    fun login_success_savesCredentialsAndReturnsSuccess() = runTest {
        whenever(apiService.login(any()))
            .thenReturn(Response.success(AuthResponseDto("tok-1", "uid-1")))

        val result = repository.login("a@b.com", "pass")

        assertTrue(result is AuthResult.Success)
        verify(tokenManager).saveToken("tok-1")
        verify(tokenManager).saveUserId("uid-1")
        verify(tokenManager).saveEmail("a@b.com")
    }

    @Test
    fun login_401_returnsCredenzaliNonValideError() = runTest {
        whenever(apiService.login(any()))
            .thenReturn(Response.error(401, "".toResponseBody()))

        val result = repository.login("a@b.com", "wrong")

        assertTrue(result is AuthResult.Error)
        assertEquals("Credenziali non valide", (result as AuthResult.Error).message)
    }

    @Test
    fun login_serverError_returnsGenericError() = runTest {
        whenever(apiService.login(any()))
            .thenReturn(Response.error(500, "".toResponseBody()))

        val result = repository.login("a@b.com", "pass")

        assertTrue(result is AuthResult.Error)
        assertEquals("Errore del server", (result as AuthResult.Error).message)
    }

    @Test
    fun login_networkException_returnsNetworkError() = runTest {
        whenever(apiService.login(any())).thenThrow(RuntimeException("timeout"))

        val result = repository.login("a@b.com", "pass")

        assertTrue(result is AuthResult.Error)
        assertTrue((result as AuthResult.Error).message.startsWith("Errore di rete"))
    }

    @Test
    fun register_success_savesCredentialsAndReturnsSuccess() = runTest {
        whenever(apiService.register(any()))
            .thenReturn(Response.success(AuthResponseDto("tok-2", "uid-2")))

        val result = repository.register("new@b.com", "pass")

        assertTrue(result is AuthResult.Success)
        verify(tokenManager).saveToken("tok-2")
        verify(tokenManager).saveUserId("uid-2")
        verify(tokenManager).saveEmail("new@b.com")
    }

    @Test
    fun register_409_returnsEmailGiaRegistrataError() = runTest {
        whenever(apiService.register(any()))
            .thenReturn(Response.error(409, "".toResponseBody()))

        val result = repository.register("used@b.com", "pass")

        assertTrue(result is AuthResult.Error)
        assertEquals("Email già registrata", (result as AuthResult.Error).message)
    }

    @Test
    fun register_400_returnsDatiNonValidiError() = runTest {
        whenever(apiService.register(any()))
            .thenReturn(Response.error(400, "".toResponseBody()))

        val result = repository.register("bad", "pass")

        assertTrue(result is AuthResult.Error)
        assertEquals("Dati non validi", (result as AuthResult.Error).message)
    }

    @Test
    fun logout_clearsToken() = runTest {
        repository.logout()

        verify(tokenManager).clearToken()
    }
}
