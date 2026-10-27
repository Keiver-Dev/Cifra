package com.Vlynk.cifra.ui.tools

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * State for the TipSplit calculator.
 *
 * @property total      Bill total string (raw input)
 * @property tipPercent Tip percentage string (raw input)
 * @property people     Number of people string (raw input)
 * @property perPerson  Calculated amount per person (null = not yet calculated or error)
 * @property tipTotal   Total tip amount
 * @property grandTotal Bill + tip total
 * @property error      Validation error message (null = no error)
 */
data class TipSplitUiState(
    val total: String = "",
    val tipPercent: String = "15",
    val people: String = "2",
    val perPerson: String? = null,
    val tipTotal: String? = null,
    val grandTotal: String? = null,
    val error: String? = null
)

/**
 * ViewModel for the Tip & Split calculator.
 * Uses [BigDecimal] for accurate monetary arithmetic.
 */
class TipSplitViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TipSplitUiState())
    val uiState: StateFlow<TipSplitUiState> = _uiState.asStateFlow()

    fun onTotalChange(value: String) {
        _uiState.update { it.copy(total = value, perPerson = null, tipTotal = null, grandTotal = null, error = null) }
    }

    fun onTipPercentChange(value: String) {
        _uiState.update { it.copy(tipPercent = value, perPerson = null, tipTotal = null, grandTotal = null, error = null) }
    }

    fun onPeopleChange(value: String) {
        _uiState.update { it.copy(people = value, perPerson = null, tipTotal = null, grandTotal = null, error = null) }
    }

    /** Runs the calculation and updates the result fields, or sets [TipSplitUiState.error]. */
    fun calculate(
        errorInvalidMsg: String,
        errorPeopleMsg: String
    ) {
        val state = _uiState.value

        val total = state.total.replace(",", ".").toBigDecimalOrNull()
        val tipPct = state.tipPercent.replace(",", ".").toBigDecimalOrNull()
        val people = state.people.trim().toIntOrNull()

        if (total == null || tipPct == null || people == null || total < BigDecimal.ZERO || tipPct < BigDecimal.ZERO) {
            _uiState.update { it.copy(error = errorInvalidMsg) }
            return
        }
        if (people < 1) {
            _uiState.update { it.copy(error = errorPeopleMsg) }
            return
        }

        val tipAmount = total.multiply(tipPct).divide(BigDecimal("100"), 2, RoundingMode.HALF_UP)
        val grand = total.add(tipAmount)
        val perPerson = grand.divide(BigDecimal(people), 2, RoundingMode.HALF_UP)

        _uiState.update {
            it.copy(
                perPerson = formatAmount(perPerson),
                tipTotal = formatAmount(tipAmount),
                grandTotal = formatAmount(grand),
                error = null
            )
        }
    }

    fun reset() {
        _uiState.value = TipSplitUiState()
    }

    private fun formatAmount(bd: BigDecimal): String =
        "%,.2f".format(bd.toDouble())
}
