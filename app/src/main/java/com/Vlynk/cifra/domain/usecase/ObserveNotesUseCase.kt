package com.Vlynk.cifra.domain.usecase

import com.Vlynk.cifra.data.repository.NoteRepository
import com.Vlynk.cifra.domain.model.Note
import kotlinx.coroutines.flow.Flow

/**
 * Use case to observe notes from the vault with optional search filtering.
 */
class ObserveNotesUseCase(
    private val repository: NoteRepository
) {
    /**
     * Observes all notes ordered by last updated descending.
     */
    operator fun invoke(): Flow<List<Note>> = repository.observeNotes()

    /**
     * Observes notes matching the specified search [query].
     */
    fun search(query: String): Flow<List<Note>> {
        return if (query.isBlank()) {
            repository.observeNotes()
        } else {
            repository.searchNotes(query.trim())
        }
    }
}
