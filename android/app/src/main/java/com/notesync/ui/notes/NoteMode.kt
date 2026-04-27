package com.notesync.ui.notes

sealed class NoteMode {
    object Create : NoteMode()
    data class Edit(val noteId: String) : NoteMode()
}
