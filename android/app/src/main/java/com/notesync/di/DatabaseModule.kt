package com.notesync.di

import androidx.room.Room
import com.notesync.data.local.NoteDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

// 'module' è la DSL di Koin per dichiarare le dipendenze.
// Non servono annotazioni: è puro Kotlin.
val databaseModule = module {
    // single: NoteDatabase viene creato una sola volta in tutta l'app.
    // androidContext() è disponibile perché abbiamo chiamato androidContext()
    // in startKoin dentro NoteApplication.
    single {
        Room.databaseBuilder(
            androidContext(),
            NoteDatabase::class.java,
            "note_database"
        ).fallbackToDestructiveMigration().build()
    }
    // single: NoteDao viene ottenuto dal database già creato sopra.
    // get() recupera automaticamente l'istanza di NoteDatabase definita sopra.
    single {
        get<NoteDatabase>().noteDao()
    }
}