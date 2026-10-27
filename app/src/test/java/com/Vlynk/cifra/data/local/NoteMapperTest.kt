package com.Vlynk.cifra.data.local

import com.Vlynk.cifra.domain.model.Note
import org.junit.Assert.assertEquals
import org.junit.Test

class NoteMapperTest {

    @Test
    fun `map entity to domain`() {
        val entity = NoteEntity(
            id = 42L,
            title = "My Secret",
            content = "Encrypted data",
            createdAt = 1000L,
            updatedAt = 2000L,
            category = "Personal"
        )

        val domain = entity.toDomain()
        assertEquals(42L, domain.id)
        assertEquals("My Secret", domain.title)
        assertEquals("Encrypted data", domain.content)
        assertEquals(1000L, domain.createdAt)
        assertEquals(2000L, domain.updatedAt)
        assertEquals("Personal", domain.category)
    }

    @Test
    fun `map domain to entity`() {
        val domain = Note(
            id = 10L,
            title = "Shopping",
            content = "Milk, Bread",
            createdAt = 500L,
            updatedAt = 800L,
            category = "Errands"
        )

        val entity = domain.toEntity()
        assertEquals(10L, entity.id)
        assertEquals("Shopping", entity.title)
        assertEquals("Milk, Bread", entity.content)
        assertEquals(500L, entity.createdAt)
        assertEquals(800L, entity.updatedAt)
        assertEquals("Errands", entity.category)
    }
}
