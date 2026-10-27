package com.Vlynk.cifra.domain.usecase

import com.Vlynk.cifra.data.repository.PinRepository

/**
 * Verifies a candidate PIN against the stored hash using constant-time comparison.
 * Returns `true` if the PIN matches, `false` otherwise (including when no PIN is set).
 */
class VerifyPinUseCase(private val repository: PinRepository) {
    suspend operator fun invoke(pin: String): Boolean = repository.verify(pin)
}
