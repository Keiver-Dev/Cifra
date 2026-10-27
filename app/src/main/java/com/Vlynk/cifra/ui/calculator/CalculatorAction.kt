package com.Vlynk.cifra.ui.calculator

/**
 * User actions supported by the Calculator screen and History sheet.
 */
sealed interface CalculatorAction {
    /** Digits 0 through 9 */
    data class Digit(val digit: Int) : CalculatorAction

    /** Binary operators (+, −, ×, ÷) */
    data class Operator(val symbol: String) : CalculatorAction

    /** Decimal point (.) */
    data object Decimal : CalculatorAction

    /** Percent operator (%) */
    data object Percent : CalculatorAction

    /** Toggle sign (±) */
    data object ToggleSign : CalculatorAction

    /** Clear all input (C) */
    data object Clear : CalculatorAction

    /** Backspace delete */
    data object Backspace : CalculatorAction

    /** Evaluate expression (=) */
    data object Equals : CalculatorAction

    /** Smart parenthesis toggle () */
    data object Parentheses : CalculatorAction

    /** Reuse a past result from history */
    data class ReuseResult(val result: String) : CalculatorAction

    /** Clear all history records */
    data object ClearHistory : CalculatorAction
}
