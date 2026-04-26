package com.notesync.navigation

sealed class NavigationRoute(val route: String) {
    object Login : NavigationRoute("login")
    object Notes : NavigationRoute("notes")
    object NoteDetail : NavigationRoute("detail/{noteId}") {
        fun createRoute(noteId: String) = "detail/$noteId"
    }
}
