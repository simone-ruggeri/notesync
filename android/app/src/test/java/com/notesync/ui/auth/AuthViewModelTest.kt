package com.notesync.ui.auth

import com.notesync.data.repository.AuthRepository
import com.notesync.data.repository.AuthResult
import com.notesync.util.MainDispatcherRule
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class AuthViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val authRepository = mock<AuthRepository>()
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setup() {
        viewModel = AuthViewModel(authRepository)
    }

    @Test
    fun login_success_setsIsSuccessTrue() = runTest(dispatcherRule.testDispatcher) {
        whenever(authRepository.login("a@b.com", "pass")).thenReturn(AuthResult.Success(Unit))

        viewModel.login("a@b.com", "pass")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isSuccess)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun login_error_setsErrorMessage() = runTest(dispatcherRule.testDispatcher) {
        whenever(authRepository.login("a@b.com", "wrong"))
            .thenReturn(AuthResult.Error("Credenziali non valide"))

        viewModel.login("a@b.com", "wrong")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Credenziali non valide", state.error)
        assertFalse(state.isSuccess)
        assertFalse(state.isLoading)
    }

    @Test
    fun login_setsIsLoadingFalseAfterCompletion() = runTest(dispatcherRule.testDispatcher) {
        whenever(authRepository.login(any(), any())).thenReturn(AuthResult.Success(Unit))

        viewModel.login("a@b.com", "pass")
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun register_success_setsIsSuccessTrue() = runTest(dispatcherRule.testDispatcher) {
        whenever(authRepository.register("new@b.com", "pass")).thenReturn(AuthResult.Success(Unit))

        viewModel.register("new@b.com", "pass")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSuccess)
    }

    @Test
    fun register_emailTaken_setsCorrectError() = runTest(dispatcherRule.testDispatcher) {
        whenever(authRepository.register("used@b.com", "pass"))
            .thenReturn(AuthResult.Error("Email già registrata"))

        viewModel.register("used@b.com", "pass")
        advanceUntilIdle()

        assertEquals("Email già registrata", viewModel.uiState.value.error)
    }

    @Test
    fun clearError_resetsErrorToNull() = runTest(dispatcherRule.testDispatcher) {
        whenever(authRepository.login(any(), any())).thenReturn(AuthResult.Error("Errore"))
        viewModel.login("x@y.com", "p")
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.error != null)

        viewModel.clearError()

        assertNull(viewModel.uiState.value.error)
    }
}
