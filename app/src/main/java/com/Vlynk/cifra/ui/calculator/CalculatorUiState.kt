package com.Vlynk.cifra.ui.calculator

import androidx.annotation.StringRes
import com.Vlynk.cifra.domain.model.HistoryEntry

/**
 * UI State for the Calculator screen.
 *
 * @property expression Infix mathematical expression currently being edited or displayed.
 * @property result Evaluated result string.
 * @property error String fallback for error message.
 * @property errorRes String resource ID from strings.xml for localized error display.
 * @property history Calculation history list, newest first.
 * @property isResultShowing True when the current state represents an evaluated result.
 */
data class CalculatorUiState(
    val expression: String = "",
    val result: String = "",
    val error: String? = null,
    @field:StringRes val errorRes: Int? = null,
    val history: List<HistoryEntry> = emptyList(),
    val isResultShowing: Boolean = false
)
