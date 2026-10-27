package com.Vlynk.cifra.domain.usecase

import com.Vlynk.cifra.data.repository.NoteRepository
import com.Vlynk.cifra.domain.model.Note

/**
 * Use case to create or update a secret note in the vault.
 * Automatically sets createdAt on creation, and updates updatedAt on every save.
 */
class SaveNoteUseCase(
    private val repository: NoteRepository
) {
    /**
     * Persists [note]. Sets createdAt if new, and always updates updatedAt to current time.
     * @return The persisted note's ID.
     */
    suspend operator fun invoke(note: Note): Long {
        val now = System.currentTimeMillis()
        val noteToSave = if (note.id == 0L) {
            note.copy(
                createdAt = if (note.createdAt == 0L) now else note.createdAt,
                updatedAt = now
            )
        } else {
            note.copy(
                updatedAt = now
            )
        }
        return repository.saveNote(noteToSave)
    }

    suspend operator fun invoke(
        title: String,
        content: String,
        id: Long = 0L,
        createdAt: Long = 0L,
        category: String? = null
    ): Long {
        return invoke(
            Note(
                id = id,
                title = title,
                content = content,
                createdAt = createdAt,
                category = category
            )
        )
    }
}
