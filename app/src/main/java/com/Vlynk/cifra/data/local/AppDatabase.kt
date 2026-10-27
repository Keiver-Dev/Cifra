package com.Vlynk.cifra.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Room database for Cifra containing encrypted/vault data tables.
 */
@Database(
    entities = [NoteEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}
