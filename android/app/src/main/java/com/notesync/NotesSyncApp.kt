package com.notesync

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import com.notesync.di.databaseModule
import com.notesync.di.networkModule
import com.notesync.di.appModule

class NotesSyncApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // startKoin avvia il container DI di Koin.
        // A differenza di Hilt, non c'è nessuna annotazione da aggiungere:
        // basta chiamare questa funzione nell'Application.
        startKoin {
            // androidLogger: mostra i log di Koin in debug (utile in sviluppo)
            androidLogger(Level.DEBUG)
            // androidContext: fornisce il Context dell'Application a tutti i moduli
            androidContext(this@NotesSyncApp)
            // modules: lista dei moduli che contengono le definizioni delle dipendenze
            modules(
                databaseModule,
                networkModule,
                appModule
            )
        }
    }
}