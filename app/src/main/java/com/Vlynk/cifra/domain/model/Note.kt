package com.Vlynk.cifra.domain.model

/**
 * Domain model representing a secret note in the vault.
 *
 * @property id Unique identifier (0 for new notes).
 * @property title Title of the note.
 * @property content Body text of the note.
 * @property createdAt Timestamp in milliseconds when the note was created.
 * @property updatedAt Timestamp in milliseconds when the note was last updated.
 * @property category Optional folder/tag category (e.g. "Home", "Work").
 */
data class Note(
    val id: Long = 0L,
    val title: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val category: String? = null
)
