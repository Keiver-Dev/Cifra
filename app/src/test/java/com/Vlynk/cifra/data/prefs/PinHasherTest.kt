package com.Vlynk.cifra.data.prefs

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Arrays

class PinHasherTest {

    private lateinit var hasher: PinHasher

    @Before
    fun setUp() {
        hasher = PinHasher()
    }

    @Test
    fun `same salt and same pin produces same hash`() {
        val pin = "1234"
        val salt = hasher.generateSalt()

        val hash1 = hasher.hash(pin, salt)
        val hash2 = hasher.hash(pin, salt)

        assertTrue(Arrays.equals(hash1, hash2))
    }

    @Test
    fun `different pin produces different hash`() {
        val salt = hasher.generateSalt()

        val hash1 = hasher.hash("1234", salt)
        val hash2 = hasher.hash("5678", salt)

        assertFalse(Arrays.equals(hash1, hash2))
    }

    @Test
    fun `different salt produces different hash for same pin`() {
        val pin = "1234"
        val salt1 = hasher.generateSalt()
        val salt2 = hasher.generateSalt()

        val hash1 = hasher.hash(pin, salt1)
        val hash2 = hasher.hash(pin, salt2)

        assertFalse(Arrays.equals(hash1, hash2))
    }

    @Test
    fun `verify returns true when pin matches`() {
        val pin = "987654"
        val salt = hasher.generateSalt()
        val hash = hasher.hash(pin, salt)

        assertTrue(hasher.verify(pin, salt, hash))
    }

    @Test
    fun `verify returns false when pin does not match`() {
        val pin = "1234"
        val wrongPin = "1235"
        val salt = hasher.generateSalt()
        val hash = hasher.hash(pin, salt)

        assertFalse(hasher.verify(wrongPin, salt, hash))
    }

    @Test
    fun `generated salt is 16 bytes and random`() {
        val salt1 = hasher.generateSalt()
        val salt2 = hasher.generateSalt()

        assertEquals(16, salt1.size)
        assertEquals(16, salt2.size)
        assertFalse(Arrays.equals(salt1, salt2))
    }

    @Test
    fun `hex conversion roundtrip is consistent`() {
        val salt = hasher.generateSalt()
        val hex = hasher.toHex(salt)
        val restored = hasher.fromHex(hex)

        assertTrue(Arrays.equals(salt, restored))
    }
}
