package com.Vlynk.cifra.domain.usecase

import com.Vlynk.cifra.data.repository.NoteRepository
import com.Vlynk.cifra.domain.model.Note

/**
 * Use case to delete a note from the vault by id or instance.
 */
class DeleteNoteUseCase(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(id: Long) {
        repository.deleteNote(id)
    }

    suspend operator fun invoke(note: Note) {
        repository.deleteNote(note)
    }
}
