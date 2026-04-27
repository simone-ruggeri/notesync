package com.notesync.di

import com.notesync.data.repository.AuthRepository
import com.notesync.data.repository.NoteRepository
import com.notesync.ui.auth.AuthViewModel
import com.notesync.ui.notes.NoteDetailViewModel
import com.notesync.ui.notes.NotesViewModel
import com.notesync.util.TokenManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // TokenManager dipende da Context:
    // androidContext() fornisce il Context dell'Application.
    single { TokenManager(androidContext()) }
    // NoteRepository riceve NoteDao, ApiService e TokenManager tramite get().
    // Koin li recupera automaticamente dalle definizioni nei moduli precedenti.
    single {
        NoteRepository(
            noteDao = get(),
            apiService = get(),
            tokenManager = get(),
            context = androidContext()
        )
    }
    // AuthRepository riceve ApiService e TokenManager.
    single {
        AuthRepository(
            apiService = get(),
            tokenManager = get()
        )
    }
    viewModel { NotesViewModel(get()) }
    viewModel { AuthViewModel(get()) }
    viewModel { params -> NoteDetailViewModel(get(), params.get()) }
}