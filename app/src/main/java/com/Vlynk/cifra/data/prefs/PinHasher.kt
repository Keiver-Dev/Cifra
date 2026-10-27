package com.Vlynk.cifra.data.prefs

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Handles secure PIN hashing using PBKDF2WithHmacSHA256.
 * - Random 16-byte salt via SecureRandom.
 * - 310 000 PBKDF2 iterations (OWASP 2023 recommendation for SHA-256).
 * - 256-bit derived key.
 * - Constant-time comparison via MessageDigest.isEqual to prevent timing attacks.
 */
class PinHasher {

    companion object {
        private const val ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val ITERATIONS = 310_000
        private const val KEY_LENGTH_BITS = 256
        private const val SALT_BYTES = 16
    }

    /** Generates a cryptographically random 16-byte salt. */
    fun generateSalt(): ByteArray {
        val salt = ByteArray(SALT_BYTES)
        SecureRandom().nextBytes(salt)
        return salt
    }

    /**
     * Hashes [pin] with the provided [salt] using PBKDF2WithHmacSHA256.
     * @return Raw hash bytes (32 bytes / 256 bits).
     */
    fun hash(pin: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(
            pin.toCharArray(),
            salt,
            ITERATIONS,
            KEY_LENGTH_BITS
        )
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        return factory.generateSecret(spec).encoded
    }

    /**
     * Verifies [pin] against a previously stored [salt] + [storedHash] in constant time.
     */
    fun verify(pin: String, salt: ByteArray, storedHash: ByteArray): Boolean {
        val candidate = hash(pin, salt)
        return MessageDigest.isEqual(candidate, storedHash)
    }

    /** Converts a byte array to its lowercase hex string representation. */
    fun toHex(bytes: ByteArray): String =
        bytes.joinToString("") { "%02x".format(it) }

    /** Converts a hex string back to a byte array. */
    fun fromHex(hex: String): ByteArray {
        require(hex.length % 2 == 0) { "Hex string must have even length" }
        return ByteArray(hex.length / 2) { i ->
            hex.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }
    }
}
