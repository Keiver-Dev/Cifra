package com.Vlynk.cifra.ui.calculator

import com.Vlynk.cifra.data.repository.HistoryRepository
import com.Vlynk.cifra.domain.calculator.Evaluator
import com.Vlynk.cifra.domain.calculator.NumberFormatter
import com.Vlynk.cifra.domain.model.HistoryEntry
import com.Vlynk.cifra.domain.usecase.EvaluateExpressionUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CalculatorViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var historyRepository: HistoryRepository
    private lateinit var evaluateExpressionUseCase: EvaluateExpressionUseCase
    private lateinit var viewModel: CalculatorViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        historyRepository = HistoryRepository()
        evaluateExpressionUseCase = EvaluateExpressionUseCase(
            evaluator = Evaluator(),
            formatter = NumberFormatter(),
            historyRepository = historyRepository
        )
        viewModel = CalculatorViewModel(
            evaluateExpressionUseCase = evaluateExpressionUseCase,
            historyRepository = historyRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is blank expression and empty result`() {
        val state = viewModel.uiState.value
        assertEquals("", state.expression)
        assertEquals("", state.result)
        assertNull(state.error)
        assertFalse(state.isResultShowing)
    }

    @Test
    fun `typing digits appends to expression`() {
        viewModel.onAction(CalculatorAction.Digit(1))
        viewModel.onAction(CalculatorAction.Digit(2))
        viewModel.onAction(CalculatorAction.Digit(3))

        assertEquals("123", viewModel.uiState.value.expression)
    }

    @Test
    fun `initial zero does not duplicate`() {
        viewModel.onAction(CalculatorAction.Digit(0))
        viewModel.onAction(CalculatorAction.Digit(0))

        assertEquals("0", viewModel.uiState.value.expression)
    }

    @Test
    fun `digit replaces initial zero`() {
        viewModel.onAction(CalculatorAction.Digit(0))
        viewModel.onAction(CalculatorAction.Digit(5))

        assertEquals("5", viewModel.uiState.value.expression)
    }

    @Test
    fun `decimal after initial zero produces zero point`() {
        viewModel.onAction(CalculatorAction.Digit(0))
        viewModel.onAction(CalculatorAction.Decimal)

        assertEquals("0.", viewModel.uiState.value.expression)
    }

    @Test
    fun `decimal on empty expression produces zero point`() {
        viewModel.onAction(CalculatorAction.Decimal)

        assertEquals("0.", viewModel.uiState.value.expression)
    }

    @Test
    fun `cannot insert multiple decimals in the same number segment`() {
        viewModel.onAction(CalculatorAction.Digit(3))
        viewModel.onAction(CalculatorAction.Decimal)
        viewModel.onAction(CalculatorAction.Digit(1))
        viewModel.onAction(CalculatorAction.Decimal)
        viewModel.onAction(CalculatorAction.Digit(4))

        assertEquals("3.14", viewModel.uiState.value.expression)
    }

    @Test
    fun `can insert decimal in subsequent number segment after operator`() {
        viewModel.onAction(CalculatorAction.Digit(1))
        viewModel.onAction(CalculatorAction.Decimal)
        viewModel.onAction(CalculatorAction.Digit(5))
        viewModel.onAction(CalculatorAction.Operator("+"))
        viewModel.onAction(CalculatorAction.Digit(2))
        viewModel.onAction(CalculatorAction.Decimal)
        viewModel.onAction(CalculatorAction.Digit(5))

        assertEquals("1.5 + 2.5", viewModel.uiState.value.expression)
    }

    @Test
    fun `consecutive operators replace previous operator`() {
        viewModel.onAction(CalculatorAction.Digit(5))
        viewModel.onAction(CalculatorAction.Operator("+"))
        assertEquals("5 + ", viewModel.uiState.value.expression)

        viewModel.onAction(CalculatorAction.Operator("×"))
        assertEquals("5 × ", viewModel.uiState.value.expression)

        viewModel.onAction(CalculatorAction.Operator("÷"))
        assertEquals("5 ÷ ", viewModel.uiState.value.expression)

        viewModel.onAction(CalculatorAction.Digit(2))
        assertEquals("5 ÷ 2", viewModel.uiState.value.expression)
    }

    @Test
    fun `equals evaluates expression and updates state`() = runTest {
        viewModel.onAction(CalculatorAction.Digit(2))
        viewModel.onAction(CalculatorAction.Operator("+"))
        viewModel.onAction(CalculatorAction.Digit(3))
        viewModel.onAction(CalculatorAction.Operator("×"))
        viewModel.onAction(CalculatorAction.Digit(4))

        viewModel.onAction(CalculatorAction.Equals)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("14", state.result)
        assertNull(state.error)
        assertTrue(state.isResultShowing)
    }

    @Test
    fun `equals on division by zero displays error and preserves expression`() = runTest {
        viewModel.onAction(CalculatorAction.Digit(1))
        viewModel.onAction(CalculatorAction.Digit(0))
        viewModel.onAction(CalculatorAction.Operator("÷"))
        viewModel.onAction(CalculatorAction.Digit(0))

        viewModel.onAction(CalculatorAction.Equals)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("10 ÷ 0", state.expression)
        assertEquals("Can't divide by 0", state.error)
        assertFalse(state.isResultShowing)
    }

    @Test
    fun `typing digit after result starts new expression`() = runTest {
        viewModel.onAction(CalculatorAction.Digit(2))
        viewModel.onAction(CalculatorAction.Operator("+"))
        viewModel.onAction(CalculatorAction.Digit(2))
        viewModel.onAction(CalculatorAction.Equals)
        advanceUntilIdle()

        assertEquals("4", viewModel.uiState.value.result)

        // Typing digit should start fresh
        viewModel.onAction(CalculatorAction.Digit(9))
        val state = viewModel.uiState.value
        assertEquals("9", state.expression)
        assertEquals("", state.result)
        assertFalse(state.isResultShowing)
    }

    @Test
    fun `typing operator after result continues with previous result`() = runTest {
        viewModel.onAction(CalculatorAction.Digit(5))
        viewModel.onAction(CalculatorAction.Operator("×"))
        viewModel.onAction(CalculatorAction.Digit(2))
        viewModel.onAction(CalculatorAction.Equals)
        advanceUntilIdle()

        assertEquals("10", viewModel.uiState.value.result)

        // Typing operator should continue with "10 + "
        viewModel.onAction(CalculatorAction.Operator("+"))
        val state = viewModel.uiState.value
        assertEquals("10 + ", state.expression)
        assertEquals("", state.result)
        assertFalse(state.isResultShowing)
    }

    @Test
    fun `backspace after result clears all`() = runTest {
        viewModel.onAction(CalculatorAction.Digit(7))
        viewModel.onAction(CalculatorAction.Operator("+"))
        viewModel.onAction(CalculatorAction.Digit(3))
        viewModel.onAction(CalculatorAction.Equals)
        advanceUntilIdle()

        viewModel.onAction(CalculatorAction.Backspace)
        val state = viewModel.uiState.value
        assertEquals("", state.expression)
        assertEquals("", state.result)
        assertFalse(state.isResultShowing)
    }

    @Test
    fun `backspace during editing deletes last character or operator`() {
        viewModel.onAction(CalculatorAction.Digit(1))
        viewModel.onAction(CalculatorAction.Digit(2))
        viewModel.onAction(CalculatorAction.Backspace)
        assertEquals("1", viewModel.uiState.value.expression)

        viewModel.onAction(CalculatorAction.Operator("+"))
        assertEquals("1 + ", viewModel.uiState.value.expression)

        viewModel.onAction(CalculatorAction.Backspace)
        assertEquals("1", viewModel.uiState.value.expression)
    }

    @Test
    fun `clear resets state`() {
        viewModel.onAction(CalculatorAction.Digit(1))
        viewModel.onAction(CalculatorAction.Operator("+"))
        viewModel.onAction(CalculatorAction.Digit(2))

        viewModel.onAction(CalculatorAction.Clear)
        val state = viewModel.uiState.value
        assertEquals("", state.expression)
        assertEquals("", state.result)
        assertNull(state.error)
    }

    @Test
    fun `reuse result replaces expression`() {
        viewModel.onAction(CalculatorAction.ReuseResult("312.5"))
        val state = viewModel.uiState.value
        assertEquals("312.5", state.expression)
        assertEquals("", state.result)
    }

    @Test
    fun `parentheses button alternates opening and closing`() {
        // Starts with (
        viewModel.onAction(CalculatorAction.Parentheses)
        assertEquals("(", viewModel.uiState.value.expression)

        viewModel.onAction(CalculatorAction.Digit(2))
        viewModel.onAction(CalculatorAction.Operator("+"))
        viewModel.onAction(CalculatorAction.Digit(3))

        // Has open parenthesis and ends with digit -> closes with )
        viewModel.onAction(CalculatorAction.Parentheses)
        assertEquals("(2 + 3)", viewModel.uiState.value.expression)
    }

    @Test
    fun `history in state updates reactively from repository`() = runTest {
        historyRepository.addEntry(HistoryEntry("1 + 1", "2"))
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.history.size)
        assertEquals("1 + 1", viewModel.uiState.value.history[0].expression)
    }

    @Test
    fun `clear history action clears repository`() = runTest {
        historyRepository.addEntry(HistoryEntry("1 + 1", "2"))
        advanceUntilIdle()

        viewModel.onAction(CalculatorAction.ClearHistory)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.history.isEmpty())
    }
}
