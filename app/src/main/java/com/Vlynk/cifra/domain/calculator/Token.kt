package com.Vlynk.cifra.domain.calculator

import java.math.BigDecimal

/**
 * Base tokens for calculator expression parsing.
 * Pure Kotlin without any Android framework dependencies.
 */
sealed interface Token {
    data class Number(val value: BigDecimal) : Token

    enum class OperatorType(val symbol: String, val precedence: Int) {
        ADD("+", 1),
        SUBTRACT("−", 1),
        MULTIPLY("×", 2),
        DIVIDE("÷", 2),
        PERCENT("%", 3)
    }

    data class Operator(val type: OperatorType) : Token
    data object LeftParen : Token
    data object RightParen : Token
}
