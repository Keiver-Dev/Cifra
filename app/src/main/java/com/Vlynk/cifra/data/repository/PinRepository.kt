package com.Vlynk.cifra.data.repository

import com.Vlynk.cifra.data.prefs.PinHasher
import com.Vlynk.cifra.data.prefs.PinStore
import kotlinx.coroutines.flow.Flow

/**
 * Provides a high-level API for PIN management.
 * All crypto operations are delegated to [PinHasher]; persistence is via [PinStore].
 */
class PinRepository(
    private val store: PinStore,
    private val hasher: PinHasher
) {

    /** Emits `true` when a PIN has already been configured. */
    fun hasPin(): Flow<Boolean> = store.hasPin

    /**
     * Hashes [pin] with a fresh random salt and persists both (hex-encoded).
     * Overwrites any previously stored PIN.
     */
    suspend fun setPin(pin: String) {
        val salt = hasher.generateSalt()
        val hash = hasher.hash(pin, salt)
        store.save(
            hashHex = hasher.toHex(hash),
            saltHex = hasher.toHex(salt)
        )
    }

    /**
     * Returns `true` when [pin] matches the stored hash (constant-time comparison).
     * Returns `false` if no PIN has been set yet.
     */
    suspend fun verify(pin: String): Boolean {
        val hashHex = store.getHash() ?: return false
        val saltHex = store.getSalt() ?: return false
        val storedHash = hasher.fromHex(hashHex)
        val salt = hasher.fromHex(saltHex)
        return hasher.verify(pin, salt, storedHash)
    }
}
