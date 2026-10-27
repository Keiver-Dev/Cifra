package com.Vlynk.cifra.ui.vault

import com.Vlynk.cifra.data.repository.NoteRepository
import com.Vlynk.cifra.domain.model.Note
import com.Vlynk.cifra.domain.usecase.DeleteNoteUseCase
import com.Vlynk.cifra.domain.usecase.ObserveNotesUseCase
import com.Vlynk.cifra.domain.usecase.SaveNoteUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VaultViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeNoteRepository
    private lateinit var observeNotesUseCase: ObserveNotesUseCase
    private lateinit var saveNoteUseCase: SaveNoteUseCase
    private lateinit var deleteNoteUseCase: DeleteNoteUseCase
    private lateinit var viewModel: VaultViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeNoteRepository()
        observeNotesUseCase = ObserveNotesUseCase(fakeRepository)
        saveNoteUseCase = SaveNoteUseCase(fakeRepository)
        deleteNoteUseCase = DeleteNoteUseCase(fakeRepository)

        viewModel = VaultViewModel(
            observeNotesUseCase = observeNotesUseCase,
            saveNoteUseCase = saveNoteUseCase,
            deleteNoteUseCase = deleteNoteUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state observes empty notes list`() = runTest {
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertTrue(state.notes.isEmpty())
        assertEquals("", state.searchQuery)
        assertNull(state.recentlyDeletedNote)
    }

    @Test
    fun `notes are emitted when repository contains notes`() = runTest {
        val note1 = Note(id = 1L, title = "Note 1", content = "Content 1")
        val note2 = Note(id = 2L, title = "Note 2", content = "Content 2")
        fakeRepository.setNotes(listOf(note1, note2))

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.notes.size)
        assertEquals("Note 1", state.notes[0].title)
    }

    @Test
    fun `searching filters notes`() = runTest {
        val note1 = Note(id = 1L, title = "WiFi password", content = "Secret123")
        val note2 = Note(id = 2L, title = "Groceries", content = "Milk")
        fakeRepository.setNotes(listOf(note1, note2))

        advanceUntilIdle()

        viewModel.onSearchQueryChange("WiFi")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("WiFi", state.searchQuery)
        assertEquals(1, state.notes.size)
        assertEquals("WiFi password", state.notes[0].title)
    }

    @Test
    fun `delete note removes it and records it for undo`() = runTest {
        val note = Note(id = 1L, title = "To delete", content = "Delete me")
        fakeRepository.setNotes(listOf(note))
        advanceUntilIdle()

        viewModel.onDelete(note)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.notes.isEmpty())
        assertEquals(note, viewModel.uiState.value.recentlyDeletedNote)
    }

    @Test
    fun `undo delete restores recently deleted note`() = runTest {
        val note = Note(id = 1L, title = "Restorable", content = "Save me")
        fakeRepository.setNotes(listOf(note))
        advanceUntilIdle()

        viewModel.onDelete(note)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.notes.isEmpty())

        viewModel.onUndoDelete()
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.notes.size)
        assertEquals("Restorable", viewModel.uiState.value.notes[0].title)
        assertNull(viewModel.uiState.value.recentlyDeletedNote)
    }

    private class FakeNoteRepository : NoteRepository {
        private val notesFlow = MutableStateFlow<List<Note>>(emptyList())

        fun setNotes(notes: List<Note>) {
            notesFlow.value = notes
        }

        override fun observeNotes(): Flow<List<Note>> = notesFlow.asStateFlow()

        override fun searchNotes(query: String): Flow<List<Note>> {
            return notesFlow.map { list ->
                list.filter { it.title.contains(query, ignoreCase = true) || it.content.contains(query, ignoreCase = true) }
            }
        }

        override suspend fun getNote(id: Long): Note? = notesFlow.value.find { it.id == id }

        override suspend fun saveNote(note: Note): Long {
            val list = notesFlow.value.toMutableList()
            val index = list.indexOfFirst { it.id == note.id && note.id != 0L }
            if (index != -1) {
                list[index] = note
            } else {
                val newId = if (note.id == 0L) (list.size + 1).toLong() else note.id
                list.add(note.copy(id = newId))
            }
            notesFlow.value = list
            return note.id
        }

        override suspend fun deleteNote(id: Long) {
            val list = notesFlow.value.toMutableList()
            list.removeIf { it.id == id }
            notesFlow.value = list
        }

        override suspend fun deleteNote(note: Note) {
            deleteNote(note.id)
        }
    }
}
