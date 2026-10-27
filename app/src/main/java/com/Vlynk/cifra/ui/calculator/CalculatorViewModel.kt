package com.Vlynk.cifra.ui.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.Vlynk.cifra.CifraApp
import com.Vlynk.cifra.data.repository.HistoryRepository
import com.Vlynk.cifra.domain.calculator.CalculatorError
import com.Vlynk.cifra.domain.usecase.EvaluateExpressionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel managing Calculator state and input validation rules.
 */
class CalculatorViewModel(
    private val evaluateExpressionUseCase: EvaluateExpressionUseCase,
    private val historyRepository: HistoryRepository
) : ViewModel() {

    companion object {
        const val MAX_EXPRESSION_LENGTH = 60

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CifraApp)
                CalculatorViewModel(
                    evaluateExpressionUseCase = app.container.evaluateExpressionUseCase,
                    historyRepository = app.container.historyRepository
                )
            }
        }
    }

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            historyRepository.history.collect { historyList ->
                _uiState.update { it.copy(history = historyList) }
            }
        }
    }

    fun onAction(action: CalculatorAction) {
        when (action) {
            is CalculatorAction.Digit -> handleDigit(action.digit)
            is CalculatorAction.Operator -> handleOperator(action.symbol)
            is CalculatorAction.Decimal -> handleDecimal()
            is CalculatorAction.Percent -> handlePercent()
            is CalculatorAction.ToggleSign -> handleToggleSign()
            is CalculatorAction.Parentheses -> handleParentheses()
            is CalculatorAction.Clear -> handleClear()
            is CalculatorAction.Backspace -> handleBackspace()
            is CalculatorAction.Equals -> handleEquals()
            is CalculatorAction.ReuseResult -> handleReuseResult(action.result)
            is CalculatorAction.ClearHistory -> handleClearHistory()
        }
    }

    private fun handleDigit(digit: Int) {
        val current = _uiState.value

        // If a result is currently shown, typing a digit begins a fresh expression
        if (current.isResultShowing) {
            _uiState.update {
                it.copy(
                    expression = digit.toString(),
                    result = "",
                    error = null,
                    isResultShowing = false
                )
            }
            return
        }

        if (current.expression.length >= MAX_EXPRESSION_LENGTH) return

        val segment = getCurrentNumberSegment(current.expression)
        val newExpression = if (segment == "0") {
            // Initial zero does not duplicate: "0" + 0 -> "0", "0" + 5 -> replaces with 5
            if (digit == 0) {
                current.expression
            } else {
                current.expression.dropLast(1) + digit.toString()
            }
        } else {
            current.expression + digit.toString()
        }

        _uiState.update {
            it.copy(
                expression = newExpression,
                error = null
            )
        }
    }

    private fun handleDecimal() {
        val current = _uiState.value

        if (current.isResultShowing) {
            _uiState.update {
                it.copy(
                    expression = "0.",
                    result = "",
                    error = null,
                    isResultShowing = false
                )
            }
            return
        }

        if (current.expression.length >= MAX_EXPRESSION_LENGTH) return

        val segment = getCurrentNumberSegment(current.expression)

        // Cannot add two decimal points to the same number segment
        if (segment.contains(".")) return

        val newExpression = if (segment.isEmpty() || current.expression.endsWith(" ") || current.expression.endsWith("(")) {
            current.expression + "0."
        } else {
            current.expression + "."
        }

        _uiState.update {
            it.copy(
                expression = newExpression,
                error = null
            )
        }
    }

    private fun handleOperator(symbol: String) {
        val canonical = toCanonicalOperator(symbol)
        val current = _uiState.value

        // If a result is showing, continue with previous result as base
        if (current.isResultShowing && current.result.isNotBlank()) {
            _uiState.update {
                it.copy(
                    expression = "${current.result} $canonical ",
                    result = "",
                    error = null,
                    isResultShowing = false
                )
            }
            return
        }

        if (current.expression.length >= MAX_EXPRESSION_LENGTH) return

        val expr = current.expression

        if (expr.isEmpty()) {
            // Support starting with minus / negative sign
            if (canonical == "−") {
                _uiState.update { it.copy(expression = "-") }
            }
            return
        }

        // Check if expression already ends with an operator (e.g., " + " or "-")
        val operatorEndingRegex = Regex("""\s*[+\-−*×/÷]\s*$""")
        val match = operatorEndingRegex.find(expr)

        val newExpression = if (match != null) {
            // Replace the previous operator with the new one
            expr.substring(0, match.range.first) + " $canonical "
        } else if (expr.endsWith("(")) {
            if (canonical == "−") "$expr-" else expr
        } else {
            "$expr $canonical "
        }

        _uiState.update {
            it.copy(
                expression = newExpression,
                error = null,
                isResultShowing = false
            )
        }
    }

    private fun handlePercent() {
        val current = _uiState.value
        val expr = current.expression.trimEnd()
        if (expr.isEmpty() || expr.endsWith("+") || expr.endsWith("−") || expr.endsWith("×") || expr.endsWith("÷") || expr.endsWith("(")) {
            return
        }
        _uiState.update {
            it.copy(
                expression = "$expr %",
                error = null
            )
        }
    }

    private fun handleToggleSign() {
        val current = _uiState.value
        if (current.isResultShowing && current.result.isNotBlank()) {
            val toggled = if (current.result.startsWith("-")) {
                current.result.removePrefix("-")
            } else {
                "-${current.result}"
            }
            _uiState.update {
                it.copy(
                    expression = toggled,
                    result = "",
                    isResultShowing = false
                )
            }
            return
        }

        val expr = current.expression
        if (expr.isEmpty()) return

        val segment = getCurrentNumberSegment(expr)
        if (segment.isNotEmpty()) {
            val beforeSegment = expr.removeSuffix(segment)
            val toggledSegment = if (segment.startsWith("-")) {
                segment.removePrefix("-")
            } else {
                "-$segment"
            }
            _uiState.update {
                it.copy(
                    expression = beforeSegment + toggledSegment,
                    error = null
                )
            }
        }
    }

    private fun handleParentheses() {
        val current = _uiState.value
        val expr = current.expression

        val openCount = expr.count { it == '(' }
        val closeCount = expr.count { it == ')' }

        val trimmed = expr.trimEnd()
        val lastChar = trimmed.lastOrNull()

        val canClose = openCount > closeCount && (lastChar?.isDigit() == true || lastChar == '%' || lastChar == ')')

        val newExpression = if (canClose) {
            "$expr)"
        } else if (lastChar?.isDigit() == true || lastChar == ')') {
            "$expr × ("
        } else {
            "$expr("
        }

        _uiState.update {
            it.copy(
                expression = newExpression,
                error = null,
                isResultShowing = false
            )
        }
    }

    private fun handleClear() {
        _uiState.update {
            it.copy(
                expression = "",
                result = "",
                error = null,
                isResultShowing = false
            )
        }
    }

    private fun handleBackspace() {
        val current = _uiState.value

        // Backspace over an evaluated result clears the state completely
        if (current.isResultShowing) {
            _uiState.update {
                it.copy(
                    expression = "",
                    result = "",
                    error = null,
                    isResultShowing = false
                )
            }
            return
        }

        val expr = current.expression
        if (expr.isEmpty()) return

        // If ends with operator and spaces (e.g. " + "), remove operator and whitespace
        val operatorEndingRegex = Regex("""\s+[+\-−*×/÷]\s+$""")
        val match = operatorEndingRegex.find(expr)

        val newExpression = if (match != null) {
            expr.substring(0, match.range.first)
        } else {
            expr.dropLast(1)
        }

        _uiState.update {
            it.copy(
                expression = newExpression,
                error = null
            )
        }
    }

    private fun handleEquals() {
        val current = _uiState.value
        val expr = current.expression.trim()
        if (expr.isEmpty()) return

        val evaluationResult = evaluateExpressionUseCase(expr)

        evaluationResult.fold(
            onSuccess = { formattedResult ->
                _uiState.update {
                    it.copy(
                        result = formattedResult,
                        error = null,
                        isResultShowing = true
                    )
                }
            },
            onFailure = { throwable ->
                val (errorMessage, errorResId) = when (throwable) {
                    is CalculatorError.DivisionByZero -> "Can't divide by 0" to com.Vlynk.cifra.R.string.error_division_by_zero
                    is CalculatorError.NumberTooLarge -> "Number too large" to com.Vlynk.cifra.R.string.error_number_too_large
                    is CalculatorError.IncompleteExpression -> "Incomplete expression" to com.Vlynk.cifra.R.string.error_incomplete_expression
                    is CalculatorError.InvalidExpression -> "Invalid format" to com.Vlynk.cifra.R.string.error_invalid_expression
                    else -> (throwable.message ?: "Error") to com.Vlynk.cifra.R.string.error_generic
                }

                _uiState.update {
                    it.copy(
                        error = errorMessage,
                        errorRes = errorResId,
                        isResultShowing = false
                    )
                }
            }
        )
    }

    private fun handleReuseResult(result: String) {
        _uiState.update {
            it.copy(
                expression = result,
                result = "",
                error = null,
                isResultShowing = false
            )
        }
    }

    private fun handleClearHistory() {
        historyRepository.clear()
    }

    private fun getCurrentNumberSegment(expr: String): String {
        return expr.substringAfterLast(" ").substringAfterLast("(")
    }

    private fun toCanonicalOperator(symbol: String): String {
        return when (symbol) {
            "+", "＋" -> "+"
            "-", "−", "–" -> "−"
            "*", "×", "x", "X" -> "×"
            "/", "÷" -> "÷"
            else -> symbol
        }
    }
}
