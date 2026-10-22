package com.Vlynk.cifra.domain.calculator

sealed class CalculatorError(message: String) : Exception(message) {
    class DivisionByZero(message: String = "Division by zero") : CalculatorError(message)
    class IncompleteExpression(message: String = "Incomplete expression") : CalculatorError(message)
    class InvalidExpression(message: String = "Invalid expression") : CalculatorError(message)
    class NumberTooLarge(message: String = "Number too large") : CalculatorError(message)
}
