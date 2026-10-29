package com.Vlynk.cifra.domain.usecase

import com.Vlynk.cifra.data.repository.HistoryRepository
import com.Vlynk.cifra.domain.calculator.CalculatorError
import com.Vlynk.cifra.domain.calculator.Evaluator
import com.Vlynk.cifra.domain.calculator.NumberFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EvaluateExpressionUseCaseTest {

    private lateinit var historyRepository: HistoryRepository
    private lateinit var evaluator: Evaluator
    private lateinit var formatter: NumberFormatter
    private lateinit var useCase: EvaluateExpressionUseCase

    @Before
    fun setUp() {
        historyRepository = HistoryRepository()
        evaluator = Evaluator()
        formatter = NumberFormatter()
        useCase = EvaluateExpressionUseCase(
            evaluator = evaluator,
            formatter = formatter,
            historyRepository = historyRepository
        )
    }

    @Test
    fun `successful evaluation formats result and adds to history`() {
        val result = useCase("2 + 3 * 4")

        assertTrue(result.isSuccess)
        assertEquals("14", result.getOrNull())

        val history = historyRepository.history.value
        assertEquals(1, history.size)
        assertEquals("2 + 3 * 4", history[0].expression)
        assertEquals("14", history[0].result)
    }

    @Test
    fun `decimal arithmetic is formatted and added to history`() {
        val result = useCase("0.1 + 0.2")

        assertTrue(result.isSuccess)
        assertEquals("0.3", result.getOrNull())

        val history = historyRepository.history.value
        assertEquals(1, history.size)
        assertEquals("0.1 + 0.2", history[0].expression)
        assertEquals("0.3", history[0].result)
    }

    @Test
    fun `division by zero fails and does not add to history`() {
        val result = useCase("10 ÷ 0")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CalculatorError.DivisionByZero)
        assertTrue(historyRepository.history.value.isEmpty())
    }

    @Test
    fun `invalid expression fails and does not add to history`() {
        val result = useCase("2 + abc")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CalculatorError.InvalidExpression)
        assertTrue(historyRepository.history.value.isEmpty())
    }

    @Test
    fun `incomplete expression fails and does not add to history`() {
        val result = useCase("2 +")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is CalculatorError.IncompleteExpression)
        assertTrue(historyRepository.history.value.isEmpty())
    }

    @Test
    fun `evaluation with saveToHistory false formats result without adding to history`() {
        val result = useCase("1234", saveToHistory = false)

        assertTrue(result.isSuccess)
        assertEquals("1234", result.getOrNull())

        val history = historyRepository.history.value
        assertTrue(history.isEmpty())
    }
}
