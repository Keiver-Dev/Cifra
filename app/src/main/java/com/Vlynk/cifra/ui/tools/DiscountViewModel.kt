package com.Vlynk.cifra.ui.tools

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * State for the Discount calculator.
 *
 * @property price        Original price string (raw input)
 * @property discountPct  Discount percentage string (raw input)
 * @property finalPrice   Computed final price after discount (null = not calculated)
 * @property saving       Amount saved (null = not calculated)
 * @property error        Validation error (null = no error)
 */
data class DiscountUiState(
    val price: String = "",
    val discountPct: String = "",
    val finalPrice: String? = null,
    val saving: String? = null,
    val error: String? = null
)

/**
 * ViewModel for the Discount calculator.
 * Uses [BigDecimal] to avoid floating-point rounding issues.
 */
class DiscountViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DiscountUiState())
    val uiState: StateFlow<DiscountUiState> = _uiState.asStateFlow()

    fun onPriceChange(value: String) {
        _uiState.update { it.copy(price = value, finalPrice = null, saving = null, error = null) }
    }

    fun onDiscountPctChange(value: String) {
        _uiState.update { it.copy(discountPct = value, finalPrice = null, saving = null, error = null) }
    }

    fun calculate(errorInvalidMsg: String, errorRangeMsg: String) {
        val state = _uiState.value

        val price = state.price.replace(",", ".").toBigDecimalOrNull()
        val pct   = state.discountPct.replace(",", ".").toBigDecimalOrNull()

        if (price == null || pct == null || price < BigDecimal.ZERO) {
            _uiState.update { it.copy(error = errorInvalidMsg) }
            return
        }
        if (pct < BigDecimal.ZERO || pct > BigDecimal("100")) {
            _uiState.update { it.copy(error = errorRangeMsg) }
            return
        }

        val saving     = price.multiply(pct).divide(BigDecimal("100"), 2, RoundingMode.HALF_UP)
        val finalPrice = price.subtract(saving)

        _uiState.update {
            it.copy(
                finalPrice = formatAmount(finalPrice),
                saving = formatAmount(saving),
                error = null
            )
        }
    }

    fun reset() { _uiState.value = DiscountUiState() }

    private fun formatAmount(bd: BigDecimal): String = "%,.2f".format(bd.toDouble())
}
