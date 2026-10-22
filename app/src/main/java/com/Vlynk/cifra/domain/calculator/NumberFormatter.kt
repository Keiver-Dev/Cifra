package com.Vlynk.cifra.domain.calculator

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Formats BigDecimal values for display in the calculator.
 * - Strips trailing zeros after the decimal point.
 * - Uses toPlainString() to avoid scientific notation (e.g., "1E+6").
 * - Optionally applies thousands separators.
 * - Optionally limits the number of decimal digits shown.
 * Pure Kotlin without any Android framework dependencies.
 */
class NumberFormatter {

    companion object {
        private const val DEFAULT_MAX_DECIMAL_DIGITS = 10
    }

    /**
     * Formats a BigDecimal for display.
     * Strips trailing zeros and avoids scientific notation.
     * @param value The BigDecimal to format.
     * @param maxDecimalDigits Maximum decimal places shown. Defaults to [DEFAULT_MAX_DECIMAL_DIGITS].
     */
    fun format(value: BigDecimal, maxDecimalDigits: Int = DEFAULT_MAX_DECIMAL_DIGITS): String {
        val scaled = if (value.scale() > maxDecimalDigits) {
            value.setScale(maxDecimalDigits, RoundingMode.HALF_UP)
        } else {
            value
        }
        return scaled.stripTrailingZeros().toPlainString()
    }

    /**
     * Formats a BigDecimal with thousands separators (e.g., "1,234,567.89").
     * Also applies [format] rules (trailing zero stripping, no scientific notation).
     * @param value The BigDecimal to format.
     * @param maxDecimalDigits Maximum decimal places shown.
     */
    fun formatWithThousands(value: BigDecimal, maxDecimalDigits: Int = DEFAULT_MAX_DECIMAL_DIGITS): String {
        val plain = format(value, maxDecimalDigits)
        val isNegative = plain.startsWith("-")
        val unsigned = if (isNegative) plain.removePrefix("-") else plain

        val parts = unsigned.split(".")
        val integerPart = parts[0]
        val decimalPart = if (parts.size > 1) ".${parts[1]}" else ""

        val withSeparators = integerPart.reversed()
            .chunked(3)
            .joinToString(",")
            .reversed()

        return if (isNegative) "-$withSeparators$decimalPart" else "$withSeparators$decimalPart"
    }
}
