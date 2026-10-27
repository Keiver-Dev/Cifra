package com.Vlynk.cifra.ui.calculator

import com.Vlynk.cifra.data.repository.HistoryRepository
import com.Vlynk.cifra.domain.calculator.Evaluator
import com.Vlynk.cifra.domain.calculator.NumberFormatter
import com.Vlynk.cifra.domain.model.HistoryEntry
import com.Vlynk.cifra.domain.usecase.EvaluateExpressionUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
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

    // Default viewModel: no PIN configured (verifyPin always false, hasPinFlow = false)
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

    // -------------------------------------------------------------------------
    // Existing arithmetic tests (unchanged behaviour)
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // PIN integration tests (5.3)
    // -------------------------------------------------------------------------

    /**
     * Helper — creates a ViewModel with a custom PIN verifier and hasPin state.
     */
    private fun pinViewModel(
        correctPin: String? = null,   // null = no PIN stored
        storedPinExists: Boolean = correctPin != null
    ) = CalculatorViewModel(
        evaluateExpressionUseCase = evaluateExpressionUseCase,
        historyRepository = historyRepository,
        verifyPin = { entered -> entered == correctPin },
        hasPinFlow = flowOf(storedPinExists)
    )

    @Test
    fun `equals with correct PIN emits NavigateToVault and clears display`() = runTest {
        val vm = pinViewModel(correctPin = "1234")

        val events = mutableListOf<CalculatorEvent>()
        val job = launch { vm.events.toList(events) }

        vm.onAction(CalculatorAction.Digit(1))
        vm.onAction(CalculatorAction.Digit(2))
        vm.onAction(CalculatorAction.Digit(3))
        vm.onAction(CalculatorAction.Digit(4))
        vm.onAction(CalculatorAction.Equals)
        advanceUntilIdle()

        assertTrue(
            "Expected NavigateToVault event",
            events.any { it is CalculatorEvent.NavigateToVault }
        )
        // Display must be clear — no evidence of PIN in the expression
        val state = vm.uiState.value
        assertEquals("", state.expression)
        assertEquals("", state.result)
        assertFalse(state.isResultShowing)

        job.cancel()
    }

    @Test
    fun `equals with incorrect PIN evaluates normally without any hint`() = runTest {
        val vm = pinViewModel(correctPin = "9999")

        val events = mutableListOf<CalculatorEvent>()
        val job = launch { vm.events.toList(events) }

        // Type "1234" (wrong PIN) — should evaluate as the number 1 234
        vm.onAction(CalculatorAction.Digit(1))
        vm.onAction(CalculatorAction.Digit(2))
        vm.onAction(CalculatorAction.Digit(3))
        vm.onAction(CalculatorAction.Digit(4))
        vm.onAction(CalculatorAction.Equals)
        advanceUntilIdle()

        // No navigation event emitted
        assertTrue(
            "No navigation event should be emitted for wrong PIN",
            events.isEmpty()
        )
        // Result shows the number as a normal calculation
        val state = vm.uiState.value
        assertTrue("Expression must evaluate to a numeric result", state.isResultShowing)
        assertTrue("Result must be non-empty", state.result.isNotEmpty())

        job.cancel()
    }

    @Test
    fun `equals with no PIN configured emits NavigateToSetPin and clears display`() = runTest {
        val vm = pinViewModel(correctPin = null, storedPinExists = false)

        val events = mutableListOf<CalculatorEvent>()
        val job = launch { vm.events.toList(events) }

        vm.onAction(CalculatorAction.Digit(4))
        vm.onAction(CalculatorAction.Digit(5))
        vm.onAction(CalculatorAction.Digit(6))
        vm.onAction(CalculatorAction.Digit(7))
        vm.onAction(CalculatorAction.Equals)
        advanceUntilIdle()

        assertTrue(
            "Expected NavigateToSetPin event when no PIN is set",
            events.any { it is CalculatorEvent.NavigateToSetPin }
        )
        val state = vm.uiState.value
        assertEquals("", state.expression)
        assertEquals("", state.result)

        job.cancel()
    }

    @Test
    fun `pin check runs before arithmetic so a valid 4-digit number is tried as PIN first`() = runTest {
        // PIN is "2000". Expression "2000" is also a valid number.
        // Correct PIN → vault navigation, NOT arithmetic result.
        val vm = pinViewModel(correctPin = "2000")

        val events = mutableListOf<CalculatorEvent>()
        val job = launch { vm.events.toList(events) }

        vm.onAction(CalculatorAction.Digit(2))
        vm.onAction(CalculatorAction.Digit(0))
        vm.onAction(CalculatorAction.Digit(0))
        vm.onAction(CalculatorAction.Digit(0))
        vm.onAction(CalculatorAction.Equals)
        advanceUntilIdle()

        assertTrue(
            "PIN check must run before arithmetic; should navigate to vault",
            events.any { it is CalculatorEvent.NavigateToVault }
        )
        assertFalse(
            "Arithmetic result must NOT be shown when PIN matched",
            vm.uiState.value.isResultShowing
        )

        job.cancel()
    }

    @Test
    fun `expression with operator is not treated as PIN even if short`() = runTest {
        // "1+2" contains non-digit chars → plain arithmetic, no PIN path
        val vm = pinViewModel(correctPin = "123")

        val events = mutableListOf<CalculatorEvent>()
        val job = launch { vm.events.toList(events) }

        vm.onAction(CalculatorAction.Digit(1))
        vm.onAction(CalculatorAction.Operator("+"))
        vm.onAction(CalculatorAction.Digit(2))
        vm.onAction(CalculatorAction.Equals)
        advanceUntilIdle()

        assertTrue("No navigation event for non-numeric expression", events.isEmpty())
        assertTrue(vm.uiState.value.isResultShowing)
        assertEquals("3", vm.uiState.value.result)

        job.cancel()
    }

    @Test
    fun `three-digit expression is not treated as PIN (below 4 digit minimum)`() = runTest {
        val vm = pinViewModel(correctPin = "123")

        val events = mutableListOf<CalculatorEvent>()
        val job = launch { vm.events.toList(events) }

        vm.onAction(CalculatorAction.Digit(1))
        vm.onAction(CalculatorAction.Digit(2))
        vm.onAction(CalculatorAction.Digit(3))
        vm.onAction(CalculatorAction.Equals)
        advanceUntilIdle()

        assertTrue("3-digit expression must not trigger PIN", events.isEmpty())
        assertTrue(vm.uiState.value.isResultShowing)

        job.cancel()
    }

    @Test
    fun `seven-digit expression is not treated as PIN (above 6 digit maximum)`() = runTest {
        val vm = pinViewModel(correctPin = "1234567")

        val events = mutableListOf<CalculatorEvent>()
        val job = launch { vm.events.toList(events) }

        "1234567".forEach { d ->
            vm.onAction(CalculatorAction.Digit(d.digitToInt()))
        }
        vm.onAction(CalculatorAction.Equals)
        advanceUntilIdle()

        assertTrue("7-digit expression must not trigger PIN", events.isEmpty())
        assertTrue(vm.uiState.value.isResultShowing)

        job.cancel()
    }
}
