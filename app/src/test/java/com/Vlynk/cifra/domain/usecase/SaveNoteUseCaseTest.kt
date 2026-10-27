package com.Vlynk.cifra.domain.usecase

import com.Vlynk.cifra.data.repository.NoteRepository
import com.Vlynk.cifra.domain.model.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SaveNoteUseCaseTest {

    private lateinit var fakeRepository: FakeNoteRepository
    private lateinit var useCase: SaveNoteUseCase

    @Before
    fun setUp() {
        fakeRepository = FakeNoteRepository()
        useCase = SaveNoteUseCase(fakeRepository)
    }

    @Test
    fun `saving new note sets createdAt and updatedAt`() = runBlocking {
        val note = Note(
            id = 0L,
            title = "New Note",
            content = "Body",
            createdAt = 0L,
            updatedAt = 0L
        )

        val before = System.currentTimeMillis()
        useCase(note)
        val after = System.currentTimeMillis()

        val saved = fakeRepository.savedNotes.first()
        assertTrue(saved.createdAt in before..after)
        assertTrue(saved.updatedAt in before..after)
        assertEquals(saved.createdAt, saved.updatedAt)
    }

    @Test
    fun `saving existing note preserves createdAt and updates updatedAt`() = runBlocking {
        val initialCreatedAt = 1000L
        val initialUpdatedAt = 2000L
        val existingNote = Note(
            id = 5L,
            title = "Existing Note",
            content = "Updated Body",
            createdAt = initialCreatedAt,
            updatedAt = initialUpdatedAt
        )

        val before = System.currentTimeMillis()
        useCase(existingNote)
        val after = System.currentTimeMillis()

        val saved = fakeRepository.savedNotes.first()
        assertEquals(initialCreatedAt, saved.createdAt)
        assertTrue(saved.updatedAt in before..after)
        assertTrue(saved.updatedAt > initialUpdatedAt)
    }

    private class FakeNoteRepository : NoteRepository {
        val savedNotes = mutableListOf<Note>()

        override fun observeNotes(): Flow<List<Note>> = flowOf(savedNotes)
        override fun searchNotes(query: String): Flow<List<Note>> = flowOf(savedNotes)
        override suspend fun getNote(id: Long): Note? = savedNotes.find { it.id == id }

        override suspend fun saveNote(note: Note): Long {
            savedNotes.add(note)
            return if (note.id == 0L) 1L else note.id
        }

        override suspend fun deleteNote(id: Long) {
            savedNotes.removeIf { it.id == id }
        }

        override suspend fun deleteNote(note: Note) {
            savedNotes.remove(note)
        }
    }
}
