package com.Vlynk.cifra.data.repository

import com.Vlynk.cifra.domain.model.HistoryEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HistoryRepositoryTest {

    private lateinit var repository: HistoryRepository

    @Before
    fun setUp() {
        repository = HistoryRepository()
    }

    @Test
    fun `initial history is empty`() {
        assertTrue(repository.history.value.isEmpty())
    }

    @Test
    fun `addEntry adds item at the beginning of history`() {
        repository.addEntry(HistoryEntry("2 + 2", "4", 1000L))
        repository.addEntry(HistoryEntry("3 * 3", "9", 2000L))

        val list = repository.history.value
        assertEquals(2, list.size)
        assertEquals("3 * 3", list[0].expression)
        assertEquals("9", list[0].result)
        assertEquals("2 + 2", list[1].expression)
        assertEquals("4", list[1].result)
    }

    @Test
    fun `history keeps at most 50 entries and discards oldest`() {
        for (i in 1..60) {
            repository.addEntry(HistoryEntry("expr $i", "$i", i.toLong()))
        }

        val list = repository.history.value
        assertEquals(50, list.size)
        // Most recent first: 60 down to 11
        assertEquals("expr 60", list.first().expression)
        assertEquals("expr 11", list.last().expression)
    }

    @Test
    fun `clear removes all entries`() {
        repository.addEntry(HistoryEntry("1 + 1", "2"))
        repository.addEntry(HistoryEntry("2 + 2", "4"))
        assertEquals(2, repository.history.value.size)

        repository.clear()
        assertTrue(repository.history.value.isEmpty())
    }
}
