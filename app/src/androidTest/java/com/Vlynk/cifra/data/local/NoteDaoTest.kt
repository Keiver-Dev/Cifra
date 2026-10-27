package com.Vlynk.cifra.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class NoteDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var noteDao: NoteDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        noteDao = database.noteDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertAndGetById() = runBlocking {
        val entity = NoteEntity(
            title = "Test Note",
            content = "This is a secret note.",
            createdAt = 1000L,
            updatedAt = 1000L
        )
        val id = noteDao.upsert(entity)

        val retrieved = noteDao.getById(id)
        assertNotNull(retrieved)
        assertEquals("Test Note", retrieved?.title)
        assertEquals("This is a secret note.", retrieved?.content)
    }

    @Test
    fun updateNote() = runBlocking {
        val original = NoteEntity(
            title = "Original Title",
            content = "Original Content",
            createdAt = 1000L,
            updatedAt = 1000L
        )
        val id = noteDao.upsert(original)

        val updated = NoteEntity(
            id = id,
            title = "Updated Title",
            content = "Updated Content",
            createdAt = 1000L,
            updatedAt = 2000L
        )
        noteDao.upsert(updated)

        val retrieved = noteDao.getById(id)
        assertEquals("Updated Title", retrieved?.title)
        assertEquals("Updated Content", retrieved?.content)
        assertEquals(2000L, retrieved?.updatedAt)
    }

    @Test
    fun deleteNote() = runBlocking {
        val entity = NoteEntity(
            title = "To Delete",
            content = "Delete Me",
            createdAt = 1000L,
            updatedAt = 1000L
        )
        val id = noteDao.upsert(entity)
        assertNotNull(noteDao.getById(id))

        noteDao.deleteById(id)
        assertNull(noteDao.getById(id))
    }

    @Test
    fun observeAllOrderedByUpdatedAtDescending() = runBlocking {
        noteDao.upsert(NoteEntity(title = "Note 1", content = "C1", createdAt = 100L, updatedAt = 100L))
        noteDao.upsert(NoteEntity(title = "Note 2", content = "C2", createdAt = 200L, updatedAt = 500L))
        noteDao.upsert(NoteEntity(title = "Note 3", content = "C3", createdAt = 300L, updatedAt = 300L))

        val notes = noteDao.observeAll().first()
        assertEquals(3, notes.size)
        // Order: Note 2 (500L), Note 3 (300L), Note 1 (100L)
        assertEquals("Note 2", notes[0].title)
        assertEquals("Note 3", notes[1].title)
        assertEquals("Note 1", notes[2].title)
    }

    @Test
    fun searchNotesMatchesTitleOrContent() = runBlocking {
        noteDao.upsert(NoteEntity(title = "Router password", content = "123456", createdAt = 100L, updatedAt = 100L))
        noteDao.upsert(NoteEntity(title = "Trip ideas", content = "Pack sunscreen", createdAt = 200L, updatedAt = 200L))

        val results = noteDao.search("password").first()
        assertEquals(1, results.size)
        assertEquals("Router password", results[0].title)

        val resultsContent = noteDao.search("sunscreen").first()
        assertEquals(1, resultsContent.size)
        assertEquals("Trip ideas", resultsContent[0].title)
    }
}
