package com.Vlynk.cifra.domain.usecase

import com.Vlynk.cifra.data.repository.PinRepository

/**
 * Sets (or updates) the vault PIN.
 * Hashes the PIN with a fresh salt before persisting — never stores the raw value.
 */
class SetPinUseCase(private val repository: PinRepository) {
    suspend operator fun invoke(pin: String) = repository.setPin(pin)
}
