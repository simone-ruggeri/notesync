package com.notesync.navigation

sealed class NavigationRoute(val route: String) {
    object Login : NavigationRoute("login")
    object Notes : NavigationRoute("notes")
    object NoteCreate : NavigationRoute("note/create")
    object NoteEdit : NavigationRoute("note/{noteId}") {
        fun createRoute(noteId: String) = "note/$noteId"
    }
}
