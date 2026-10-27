package com.Vlynk.cifra.ui.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.Vlynk.cifra.CifraApp
import com.Vlynk.cifra.R
import com.Vlynk.cifra.data.repository.HistoryRepository
import com.Vlynk.cifra.domain.calculator.CalculatorError
import com.Vlynk.cifra.domain.usecase.EvaluateExpressionUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ---------------------------------------------------------------------------
// Navigation events emitted by the ViewModel
// ---------------------------------------------------------------------------

sealed interface CalculatorEvent {
    /** Correct PIN entered — navigate to the Vault home screen. */
    data object NavigateToVault : CalculatorEvent
    /** No PIN configured yet — navigate to PIN setup. */
    data object NavigateToSetPin : CalculatorEvent
}

/**
 * ViewModel managing Calculator state and input validation rules.
 *
 * PIN integration (5.3):
 * When `=` is pressed and the expression is purely numeric with 4–6 digits,
 * the PIN verification happens **before** the arithmetic evaluation.
 * - Correct PIN  → clear display, emit [CalculatorEvent.NavigateToVault].
 * - No PIN set   → clear display, emit [CalculatorEvent.NavigateToSetPin].
 * - Wrong PIN    → fall through to normal calculation; no hint of a vault.
 *
 * [verifyPin] and [hasPinFlow] are injected as lambdas/flows so the ViewModel
 * remains fully testable without an Android Context.
 */
class CalculatorViewModel(
    private val evaluateExpressionUseCase: EvaluateExpressionUseCase,
    private val historyRepository: HistoryRepository,
    /** Returns `true` if [pin] matches the stored hash. Defaults to always-false for tests that don't exercise the PIN path. */
    private val verifyPin: suspend (String) -> Boolean = { false },
    /** Emits whether a PIN has been configured. Defaults to `false` for non-PIN tests. */
    private val hasPinFlow: Flow<Boolean> = flowOf(false)
) : ViewModel() {

    companion object {
        const val MAX_EXPRESSION_LENGTH = 60

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CifraApp)
                CalculatorViewModel(
                    evaluateExpressionUseCase = app.container.evaluateExpressionUseCase,
                    historyRepository = app.container.historyRepository,
                    verifyPin = app.container.verifyPinUseCase::invoke,
                    hasPinFlow = app.container.pinRepository.hasPin()
                )
            }
        }
    }

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    private val _events = Channel<CalculatorEvent>(capacity = Channel.BUFFERED)
    /** One-shot navigation events. Collect in the UI with `LaunchedEffect`. */
    val events: Flow<CalculatorEvent> = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            historyRepository.history.collect { historyList ->
                _uiState.update { it.copy(history = historyList) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        _events.close()
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

    /**
     * Handles the `=` press.
     *
     * PIN check happens FIRST, before any arithmetic:
     * - Expression is purely digits, length 4–6 → treat as potential PIN.
     * - No PIN stored → navigate to SetPin silently.
     * - PIN correct   → clear display, navigate to Vault.
     * - PIN wrong     → evaluate as a number (no feedback about vault).
     * - Otherwise     → normal arithmetic evaluation.
     */
    private fun handleEquals() {
        val expr = _uiState.value.expression.trim()
        if (expr.isEmpty()) return

        if (looksLikePin(expr)) {
            // PIN path — async because DataStore reads are suspend
            viewModelScope.launch {
                val pinIsSet = hasPinFlow.first()
                if (!pinIsSet) {
                    // First run: guide user to set a PIN, leave no trace
                    clearForNavigation()
                    _events.send(CalculatorEvent.NavigateToSetPin)
                    return@launch
                }
                if (verifyPin(expr)) {
                    // ✅ Correct PIN — open the vault without a trace
                    clearForNavigation()
                    _events.send(CalculatorEvent.NavigateToVault)
                } else {
                    // ❌ Wrong PIN — evaluate normally, zero hints
                    evaluateNormally(expr)
                }
            }
        } else {
            // Pure arithmetic — synchronous, no PIN involved
            evaluateNormally(expr)
        }
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

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * A string "looks like a PIN" if it consists entirely of digits and its
     * length falls in the valid PIN range [4, 6].
     * The check happens BEFORE arithmetic so that e.g. "1234" is tried as a PIN
     * first and only falls through to arithmetic if the PIN is wrong.
     */
    private fun looksLikePin(expr: String): Boolean =
        expr.length in 4..6 && expr.all { it.isDigit() }

    /** Clears display state before navigating away so no data leaks into history. */
    private fun clearForNavigation() {
        _uiState.update {
            it.copy(expression = "", result = "", error = null, isResultShowing = false)
        }
    }

    private fun evaluateNormally(expr: String) {
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
                val (errorMessage: String, errorResId: Int) = when (throwable) {
                    is CalculatorError.DivisionByZero -> "Can't divide by 0" to R.string.error_division_by_zero
                    is CalculatorError.NumberTooLarge -> "Number too large" to R.string.error_number_too_large
                    is CalculatorError.IncompleteExpression -> "Incomplete expression" to R.string.error_incomplete_expression
                    is CalculatorError.InvalidExpression -> "Invalid format" to R.string.error_invalid_expression
                    else -> (throwable.message ?: "Error") to R.string.error_generic
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
