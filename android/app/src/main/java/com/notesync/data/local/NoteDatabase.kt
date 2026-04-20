package com.notesync.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// @Database registra le Entity (tabelle) e la versione dello schema
// Quando cambi lo schema, aumenta la version e scrivi una migration
@Database(
    entities = [NoteEntity::class],
    version = 1,
    exportSchema = true // esporta lo schema in JSON per review
)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        // @Volatile garantisce che la lettura di INSTANCE sia sempre aggiornata
        @Volatile
        private var INSTANCE: NoteDatabase? = null

        // Singleton pattern: una sola istanza del database nell'app
        fun getDatabase(context: Context): NoteDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java,
                    "note_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
}