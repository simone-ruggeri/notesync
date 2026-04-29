package com.notesync.di

import com.notesync.data.repository.AuthRepository
import com.notesync.data.repository.NoteRepository
import com.notesync.ui.auth.AuthViewModel
import com.notesync.ui.notes.NoteDetailViewModel
import com.notesync.ui.notes.NotesViewModel
import com.notesync.util.NetworkChecker
import com.notesync.util.NetworkUtils
import com.notesync.util.TokenManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { TokenManager(androidContext()) }
    single<NetworkChecker> { NetworkUtils(androidContext()) }
    single {
        NoteRepository(
            noteDao = get(),
            apiService = get(),
            tokenManager = get(),
            network = get()
        )
    }
    single {
        AuthRepository(
            apiService = get(),
            tokenManager = get()
        )
    }
    viewModel { NotesViewModel(get(), get()) }
    viewModel { AuthViewModel(get()) }
    viewModel { params -> NoteDetailViewModel(get(), params.get()) }
}
