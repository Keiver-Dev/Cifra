package com.Vlynk.cifra.ui.pin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.Vlynk.cifra.CifraApp
import com.Vlynk.cifra.domain.usecase.SetPinUseCase
import com.Vlynk.cifra.domain.usecase.VerifyPinUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Mode in which the PIN screen is opened:
 * - [Setup]: First-time PIN creation — no current PIN needed.
 * - [Change]: Change existing PIN — user must verify the current PIN first.
 */
enum class PinScreenMode { Setup, Change }

/**
 * UI state for [SetPinScreen].
 */
data class PinUiState(
    val mode: PinScreenMode = PinScreenMode.Setup,
    // Step 1 — current PIN verification (Change mode only)
    val currentPin: String = "",
    val currentPinError: String? = null,
    // Step 2 — new PIN entry
    val newPin: String = "",
    val newPinError: String? = null,
    // Step 3 — confirm new PIN
    val confirmPin: String = "",
    val confirmPinError: String? = null,
    // Which step is active in Change mode
    val isVerifyingCurrent: Boolean = false,
    // Terminal states
    val isSaved: Boolean = false,
    val isLoading: Boolean = false,
    val globalError: String? = null
)

/**
 * ViewModel for PIN setup and change flows.
 *
 * Validation rules:
 * - Only digits allowed.
 * - Length between 4 and 6 digits.
 * - New PIN and confirmation must match.
 * - In Change mode the current PIN must verify before proceeding.
 */
class PinViewModel(
    private val setPinUseCase: SetPinUseCase,
    private val verifyPinUseCase: VerifyPinUseCase,
    mode: PinScreenMode = PinScreenMode.Setup
) : ViewModel() {

    companion object {
        /** Factory for Setup mode (first-time PIN creation). */
        val SetupFactory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CifraApp
                PinViewModel(
                    setPinUseCase = app.container.setPinUseCase,
                    verifyPinUseCase = app.container.verifyPinUseCase,
                    mode = PinScreenMode.Setup
                )
            }
        }

        /** Factory for Change mode (change existing PIN). */
        val ChangeFactory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CifraApp
                PinViewModel(
                    setPinUseCase = app.container.setPinUseCase,
                    verifyPinUseCase = app.container.verifyPinUseCase,
                    mode = PinScreenMode.Change
                )
            }
        }
    }

    private val _uiState = MutableStateFlow(
        PinUiState(
            mode = mode,
            isVerifyingCurrent = mode == PinScreenMode.Change
        )
    )
    val uiState: StateFlow<PinUiState> = _uiState.asStateFlow()

    // -------------------------------------------------------------------------
    // Input handlers
    // -------------------------------------------------------------------------

    fun onCurrentPinChange(value: String) {
        if (!value.all { it.isDigit() } || value.length > 6) return
        _uiState.update { it.copy(currentPin = value, currentPinError = null, globalError = null) }
    }

    fun onNewPinChange(value: String) {
        if (!value.all { it.isDigit() } || value.length > 6) return
        _uiState.update { it.copy(newPin = value, newPinError = null) }
    }

    fun onConfirmPinChange(value: String) {
        if (!value.all { it.isDigit() } || value.length > 6) return
        _uiState.update { it.copy(confirmPin = value, confirmPinError = null) }
    }

    // -------------------------------------------------------------------------
    // Actions
    // -------------------------------------------------------------------------

    /**
     * In Change mode: verify the current PIN before moving to the new PIN step.
     * In Setup mode: this step is skipped.
     */
    fun onVerifyCurrentPin() {
        val state = _uiState.value
        val pin = state.currentPin

        val error = validateLength(pin)
        if (error != null) {
            _uiState.update { it.copy(currentPinError = error) }
            return
        }

        _uiState.update { it.copy(isLoading = true, globalError = null) }
        viewModelScope.launch {
            val correct = verifyPinUseCase(pin)
            if (correct) {
                _uiState.update { it.copy(isLoading = false, isVerifyingCurrent = false) }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentPin = "",
                        currentPinError = "PIN incorrecto. Intenta de nuevo."
                    )
                }
            }
        }
    }

    /**
     * Validates both PIN fields and, if valid, hashes and stores the PIN.
     * On success sets [PinUiState.isSaved] = true — the screen should navigate away.
     */
    fun onSave() {
        val state = _uiState.value
        val newPin = state.newPin
        val confirmPin = state.confirmPin

        var hasError = false

        val newError = validateLength(newPin)
        if (newError != null) {
            _uiState.update { it.copy(newPinError = newError) }
            hasError = true
        }

        val confirmError = validateLength(confirmPin)
        if (confirmError != null) {
            _uiState.update { it.copy(confirmPinError = confirmError) }
            hasError = true
        }

        if (!hasError && newPin != confirmPin) {
            _uiState.update { it.copy(confirmPinError = "Los PINs no coinciden.") }
            hasError = true
        }

        if (hasError) return

        _uiState.update { it.copy(isLoading = true, globalError = null) }
        viewModelScope.launch {
            try {
                setPinUseCase(newPin)
                _uiState.update { it.copy(isLoading = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, globalError = "Error al guardar el PIN. Intenta de nuevo.")
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private fun validateLength(pin: String): String? = when {
        pin.length < 4 -> "El PIN debe tener entre 4 y 6 dígitos."
        pin.length > 6 -> "El PIN debe tener entre 4 y 6 dígitos."
        else -> null
    }
}
