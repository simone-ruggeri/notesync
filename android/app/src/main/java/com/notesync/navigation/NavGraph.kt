package com.notesync.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.notesync.ui.auth.LoginScreen
import com.notesync.ui.notes.NoteDetailScreen
import com.notesync.ui.notes.NoteMode
import com.notesync.ui.notes.NotesScreen
import com.notesync.util.TokenManager

@Composable
fun NavGraph(
    navController: NavHostController,
    tokenManager: TokenManager,
    modifier: Modifier = Modifier
) {
    // null = DataStore non ha ancora emesso (avvio app, pochi ms)
    // true = utente autenticato, false = non autenticato
    // produceState gestisce automaticamente la cancellazione quando il composable esce dalla composizione.
    val isLoggedIn: Boolean? by produceState<Boolean?>(initialValue = null) {
        tokenManager.userIdFlow.collect { userId ->
            value = userId != null
        }
    }

    if (isLoggedIn == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val startDestination = if (isLoggedIn == true) {
        NavigationRoute.Notes.route
    } else {
        NavigationRoute.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(NavigationRoute.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(NavigationRoute.Notes.route) {
                        popUpTo(NavigationRoute.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(NavigationRoute.Notes.route) {
            NotesScreen(
                onNavigateToCreate = {
                    navController.navigate(NavigationRoute.NoteCreate.route)
                },
                onNavigateToEdit = { noteId ->
                    navController.navigate(NavigationRoute.NoteEdit.createRoute(noteId))
                },
                onLogout = {
                    navController.navigate(NavigationRoute.Login.route) {
                        popUpTo(NavigationRoute.Notes.route) { inclusive = true }
                    }
                }
            )
        }
        composable(NavigationRoute.NoteCreate.route) {
            NoteDetailScreen(
                mode = NoteMode.Create,
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = NavigationRoute.NoteEdit.route,
            arguments = listOf(navArgument("noteId") { type = NavType.StringType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId") ?: return@composable
            NoteDetailScreen(
                mode = NoteMode.Edit(noteId),
                onBack = { navController.popBackStack() }
            )
        }
    }
}
