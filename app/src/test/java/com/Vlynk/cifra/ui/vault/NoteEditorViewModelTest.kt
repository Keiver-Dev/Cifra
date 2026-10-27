package com.Vlynk.cifra.ui.vault

import com.Vlynk.cifra.data.repository.NoteRepository
import com.Vlynk.cifra.domain.model.Note
import com.Vlynk.cifra.domain.usecase.DeleteNoteUseCase
import com.Vlynk.cifra.domain.usecase.SaveNoteUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NoteEditorViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeNoteRepository
    private lateinit var saveNoteUseCase: SaveNoteUseCase
    private lateinit var deleteNoteUseCase: DeleteNoteUseCase

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeNoteRepository()
        saveNoteUseCase = SaveNoteUseCase(fakeRepository)
        deleteNoteUseCase = DeleteNoteUseCase(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `new note starts with blank fields`() = runTest {
        val viewModel = NoteEditorViewModel(
            noteId = null,
            noteRepository = fakeRepository,
            saveNoteUseCase = saveNoteUseCase,
            deleteNoteUseCase = deleteNoteUseCase
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(0L, state.id)
        assertEquals("", state.title)
        assertEquals("", state.content)
        assertTrue(state.isNew)
        assertTrue(state.isEmpty)
    }

    @Test
    fun `loads existing note by noteId`() = runTest {
        val note = Note(id = 12L, title = "Loaded Note", content = "Loaded Content", createdAt = 500L, updatedAt = 600L)
        fakeRepository.setNote(note)

        val viewModel = NoteEditorViewModel(
            noteId = 12L,
            noteRepository = fakeRepository,
            saveNoteUseCase = saveNoteUseCase,
            deleteNoteUseCase = deleteNoteUseCase
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(12L, state.id)
        assertEquals("Loaded Note", state.title)
        assertEquals("Loaded Content", state.content)
        assertFalse(state.isNew)
        assertFalse(state.hasUnsavedChanges)
    }

    @Test
    fun `updating title and content flags unsaved changes`() = runTest {
        val viewModel = NoteEditorViewModel(
            noteId = null,
            noteRepository = fakeRepository,
            saveNoteUseCase = saveNoteUseCase,
            deleteNoteUseCase = deleteNoteUseCase
        )
        advanceUntilIdle()

        viewModel.onTitleChange("Ideas")
        viewModel.onContentChange("My project ideas")

        val state = viewModel.uiState.value
        assertEquals("Ideas", state.title)
        assertEquals("My project ideas", state.content)
        assertTrue(state.hasUnsavedChanges)
    }

    @Test
    fun `does not save completely empty note`() = runTest {
        val viewModel = NoteEditorViewModel(
            noteId = null,
            noteRepository = fakeRepository,
            saveNoteUseCase = saveNoteUseCase,
            deleteNoteUseCase = deleteNoteUseCase
        )
        advanceUntilIdle()

        val saved = viewModel.save()
        advanceUntilIdle()

        assertFalse(saved)
        assertTrue(fakeRepository.savedNotes.isEmpty())
    }

    @Test
    fun `saving non-empty note persists it to repository`() = runTest {
        val viewModel = NoteEditorViewModel(
            noteId = null,
            noteRepository = fakeRepository,
            saveNoteUseCase = saveNoteUseCase,
            deleteNoteUseCase = deleteNoteUseCase
        )
        advanceUntilIdle()

        viewModel.onTitleChange("Secret Recipe")
        viewModel.onContentChange("Flour, sugar, butter")

        val saved = viewModel.save()
        advanceUntilIdle()

        assertTrue(saved)
        assertEquals(1, fakeRepository.savedNotes.size)
        assertEquals("Secret Recipe", fakeRepository.savedNotes.first().title)
        assertTrue(viewModel.uiState.value.isSaved)
    }

    @Test
    fun `delete removes note from repository`() = runTest {
        val note = Note(id = 7L, title = "Delete Me", content = "Bye")
        fakeRepository.setNote(note)

        val viewModel = NoteEditorViewModel(
            noteId = 7L,
            noteRepository = fakeRepository,
            saveNoteUseCase = saveNoteUseCase,
            deleteNoteUseCase = deleteNoteUseCase
        )
        advanceUntilIdle()

        viewModel.delete()
        advanceUntilIdle()

        assertTrue(fakeRepository.savedNotes.isEmpty())
        assertTrue(viewModel.uiState.value.isSaved)
    }

    private class FakeNoteRepository : NoteRepository {
        val savedNotes = mutableListOf<Note>()

        fun setNote(note: Note) {
            savedNotes.add(note)
        }

        override fun observeNotes(): Flow<List<Note>> = flowOf(savedNotes)
        override fun searchNotes(query: String): Flow<List<Note>> = flowOf(savedNotes)
        override suspend fun getNote(id: Long): Note? = savedNotes.find { it.id == id }

        override suspend fun saveNote(note: Note): Long {
            val index = savedNotes.indexOfFirst { it.id == note.id && note.id != 0L }
            if (index != -1) {
                savedNotes[index] = note
            } else {
                val newId = if (note.id == 0L) (savedNotes.size + 1).toLong() else note.id
                savedNotes.add(note.copy(id = newId))
            }
            return note.id
        }

        override suspend fun deleteNote(id: Long) {
            savedNotes.removeIf { it.id == id }
        }

        override suspend fun deleteNote(note: Note) {
            deleteNote(note.id)
        }
    }
}
