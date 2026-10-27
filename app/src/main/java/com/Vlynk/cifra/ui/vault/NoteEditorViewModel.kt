package com.Vlynk.cifra.ui.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.Vlynk.cifra.CifraApp
import com.Vlynk.cifra.data.repository.NoteRepository
import com.Vlynk.cifra.domain.model.Note
import com.Vlynk.cifra.domain.usecase.DeleteNoteUseCase
import com.Vlynk.cifra.domain.usecase.SaveNoteUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State for the Note Editor.
 */
data class NoteEditorUiState(
    val id: Long = 0L,
    val title: String = "",
    val content: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val isSaved: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val initialTitle: String = "",
    val initialContent: String = ""
) {
    val isNew: Boolean get() = id == 0L
    val isEmpty: Boolean get() = title.isBlank() && content.isBlank()
    val hasUnsavedChanges: Boolean get() = title != initialTitle || content != initialContent
    val charCount: Int get() = content.length
}

/**
 * ViewModel managing editing, validation, saving, and deletion of a vault note.
 */
class NoteEditorViewModel(
    val noteId: Long?,
    private val noteRepository: NoteRepository,
    private val saveNoteUseCase: SaveNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NoteEditorUiState())
    val uiState: StateFlow<NoteEditorUiState> = _uiState.asStateFlow()

    init {
        if (noteId != null && noteId != 0L) {
            loadNote(noteId)
        }
    }

    private fun loadNote(id: Long) {
        viewModelScope.launch {
            val note = noteRepository.getNote(id)
            if (note != null) {
                _uiState.update {
                    it.copy(
                        id = note.id,
                        title = note.title,
                        content = note.content,
                        createdAt = note.createdAt,
                        updatedAt = note.updatedAt,
                        initialTitle = note.title,
                        initialContent = note.content
                    )
                }
            }
        }
    }

    fun onTitleChange(newTitle: String) {
        _uiState.update { it.copy(title = newTitle) }
    }

    fun onContentChange(newContent: String) {
        _uiState.update { it.copy(content = newContent) }
    }

    fun save(): Boolean {
        val current = _uiState.value
        if (current.isEmpty) return false

        viewModelScope.launch {
            val note = Note(
                id = current.id,
                title = current.title.trim(),
                content = current.content.trim(),
                createdAt = current.createdAt
            )
            saveNoteUseCase(note)
            _uiState.update { it.copy(isSaved = true) }
        }
        return true
    }

    fun delete() {
        val current = _uiState.value
        if (!current.isNew) {
            viewModelScope.launch {
                deleteNoteUseCase(current.id)
                _uiState.update { it.copy(isSaved = true, showDeleteDialog = false) }
            }
        }
    }

    fun showDeleteDialog(show: Boolean) {
        _uiState.update { it.copy(showDeleteDialog = show) }
    }

    fun saveOnBack(): Boolean {
        val current = _uiState.value
        if (current.hasUnsavedChanges && !current.isEmpty) {
            return save()
        }
        return true
    }

    companion object {
        fun provideFactory(
            noteId: Long?,
            app: CifraApp
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return NoteEditorViewModel(
                    noteId = noteId,
                    noteRepository = app.container.noteRepository,
                    saveNoteUseCase = app.container.saveNoteUseCase,
                    deleteNoteUseCase = app.container.deleteNoteUseCase
                ) as T
            }
        }
    }
}
