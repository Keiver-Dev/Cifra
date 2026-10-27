package com.Vlynk.cifra.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for notes table in Room.
 */
@Dao
interface NoteDao {

    /**
     * Observes all notes ordered by last updated timestamp descending.
     */
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<NoteEntity>>

    /**
     * Fetches a single note by its ID.
     */
    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getById(id: Long): NoteEntity?

    /**
     * Searches notes whose title or content matches [query], ordered by updatedAt descending.
     */
    @Query("SELECT * FROM notes WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun search(query: String): Flow<List<NoteEntity>>

    /**
     * Inserts or updates the given note entity.
     * @return The row ID of the inserted/updated note.
     */
    @Upsert
    suspend fun upsert(note: NoteEntity): Long

    /**
     * Deletes the specified note.
     */
    @Delete
    suspend fun delete(note: NoteEntity)

    /**
     * Deletes a note by its ID.
     */
    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: Long): Int
}
