package com.Vlynk.cifra.data.repository

import com.Vlynk.cifra.data.local.NoteDao
import com.Vlynk.cifra.data.local.toDomain
import com.Vlynk.cifra.data.local.toEntity
import com.Vlynk.cifra.domain.model.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Repository contract for managing vault notes.
 */
interface NoteRepository {
    fun observeNotes(): Flow<List<Note>>
    fun searchNotes(query: String): Flow<List<Note>>
    suspend fun getNote(id: Long): Note?
    suspend fun saveNote(note: Note): Long
    suspend fun deleteNote(id: Long)
    suspend fun deleteNote(note: Note)
}

/**
 * Room-backed implementation of [NoteRepository].
 */
class NoteRepositoryImpl(
    private val noteDao: NoteDao
) : NoteRepository {

    override fun observeNotes(): Flow<List<Note>> {
        return noteDao.observeAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun searchNotes(query: String): Flow<List<Note>> {
        return noteDao.search(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getNote(id: Long): Note? = withContext(Dispatchers.IO) {
        noteDao.getById(id)?.toDomain()
    }

    override suspend fun saveNote(note: Note): Long = withContext(Dispatchers.IO + NonCancellable) {
        noteDao.upsert(note.toEntity())
    }

    override suspend fun deleteNote(id: Long) {
        withContext(Dispatchers.IO + NonCancellable) {
            noteDao.deleteById(id)
        }
    }

    override suspend fun deleteNote(note: Note) {
        withContext(Dispatchers.IO + NonCancellable) {
            noteDao.delete(note.toEntity())
        }
    }
}
