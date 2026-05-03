package com.notesync.ui.auth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.notesync.StubApiService
import com.notesync.data.repository.AuthRepository
import com.notesync.ui.theme.NoteSyncTheme
import com.notesync.util.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Response

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var stubApi: StubApiService
    private lateinit var tokenManager: TokenManager
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setup(): Unit = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        stubApi = StubApiService()
        tokenManager = TokenManager(context)
        val repository = AuthRepository(stubApi, tokenManager)
        viewModel = AuthViewModel(repository)
    }

    @After
    fun teardown(): Unit = runBlocking {
        tokenManager.clearToken()
    }

    private fun setContent() {
        composeTestRule.setContent {
            NoteSyncTheme {
                LoginScreen(onLoginSuccess = {}, viewModel = viewModel)
            }
        }
    }

    @Test
    fun loginButton_enabledByDefault() {
        setContent()
        // Button is always enabled (validation happens server-side / via HTML required)
        composeTestRule.onNodeWithTag("submit_button").assertIsEnabled()
    }

    @Test
    fun errorMessage_displayedOnLoginFailure() {
        stubApi.loginResult = Response.error(401, "".toResponseBody())
        setContent()
        composeTestRule.onAllNodes(hasSetTextAction())[0].performTextInput("wrong@test.com")
        composeTestRule.onAllNodes(hasSetTextAction())[1].performTextInput("wrongpass")
        composeTestRule.onNodeWithTag("submit_button").performClick()

        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodes(hasText("Credenziali non valide"))
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Credenziali non valide").assertIsDisplayed()
    }

    @Test
    fun tabSwitch_toRegistrati_changesButtonLabel() {
        setContent()
        composeTestRule.onNodeWithText("Registrati").performClick()
        // Button label changes to "Crea account"
        composeTestRule.onNodeWithText("Crea account").assertIsDisplayed()
    }
}
