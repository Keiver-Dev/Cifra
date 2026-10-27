package com.Vlynk.cifra.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity representing a persisted note in SQLite.
 */
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long,
    val category: String? = null
)
