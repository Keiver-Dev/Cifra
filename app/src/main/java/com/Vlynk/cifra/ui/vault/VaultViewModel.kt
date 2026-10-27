package com.Vlynk.cifra.ui.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.Vlynk.cifra.CifraApp
import com.Vlynk.cifra.domain.model.Note
import com.Vlynk.cifra.domain.usecase.DeleteNoteUseCase
import com.Vlynk.cifra.domain.usecase.ObserveNotesUseCase
import com.Vlynk.cifra.domain.usecase.SaveNoteUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * State for the Vault home screen.
 */
data class VaultUiState(
    val notes: List<Note> = emptyList(),
    val searchQuery: String = "",
    val recentlyDeletedNote: Note? = null
)

/**
 * ViewModel managing secret notes in the vault, search filtering, and deletion with undo.
 */
class VaultViewModel(
    private val observeNotesUseCase: ObserveNotesUseCase,
    private val saveNoteUseCase: SaveNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase
) : ViewModel() {

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CifraApp)
                VaultViewModel(
                    observeNotesUseCase = app.container.observeNotesUseCase,
                    saveNoteUseCase = app.container.saveNoteUseCase,
                    deleteNoteUseCase = app.container.deleteNoteUseCase
                )
            }
        }
    }

    private val _searchQuery = MutableStateFlow("")
    private val _uiState = MutableStateFlow(VaultUiState())
    val uiState: StateFlow<VaultUiState> = _uiState.asStateFlow()

    init {
        observeNotes()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeNotes() {
        viewModelScope.launch {
            _searchQuery.flatMapLatest { query ->
                observeNotesUseCase.search(query)
            }.collect { notesList ->
                _uiState.update { it.copy(notes = notesList) }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onDelete(note: Note) {
        viewModelScope.launch {
            deleteNoteUseCase(note)
            _uiState.update { it.copy(recentlyDeletedNote = note) }
        }
    }

    fun onUndoDelete() {
        val noteToRestore = _uiState.value.recentlyDeletedNote ?: return
        viewModelScope.launch {
            saveNoteUseCase(noteToRestore)
            _uiState.update { it.copy(recentlyDeletedNote = null) }
        }
    }

    fun onDismissSnackbar() {
        _uiState.update { it.copy(recentlyDeletedNote = null) }
    }
}
