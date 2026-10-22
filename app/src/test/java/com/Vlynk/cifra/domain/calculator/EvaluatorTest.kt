package com.Vlynk.cifra.domain.calculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class EvaluatorTest {

    private lateinit var evaluator: Evaluator

    @Before
    fun setUp() {
        evaluator = Evaluator()
    }

    private fun eval(expression: String): BigDecimal {
        val result = evaluator.evaluate(expression)
        assertTrue("Expected success but got: $result", result.isSuccess)
        return result.getOrThrow()
    }

    /**
     * Asserts two BigDecimal values are numerically equal (ignoring scale differences).
     * e.g. BigDecimal("0.3") == BigDecimal("0.30") → compareTo == 0
     */
    private fun assertEqualsBD(expected: BigDecimal, actual: BigDecimal) {
        assertEquals(
            "Expected $expected but got $actual",
            0,
            expected.compareTo(actual)
        )
    }

    private fun expectError(expression: String, errorClass: Class<out CalculatorError>) {
        val result = evaluator.evaluate(expression)
        assertTrue("Expected failure but got success: ${result.getOrNull()}", result.isFailure)
        val ex = result.exceptionOrNull()
        assertTrue(
            "Expected ${errorClass.simpleName} but got ${ex?.javaClass?.simpleName}",
            errorClass.isInstance(ex)
        )
    }

    // --- Basic arithmetic ---

    @Test
    fun `addition of two integers`() {
        assertEquals(BigDecimal("5"), eval("2 + 3"))
    }

    @Test
    fun `subtraction`() {
        assertEquals(BigDecimal("6"), eval("10 - 4"))
    }

    @Test
    fun `multiplication`() {
        assertEquals(BigDecimal("12"), eval("3 * 4"))
    }

    @Test
    fun `division exact`() {
        assertEquals(BigDecimal("4"), eval("12 ÷ 3"))
    }

    // --- Operator precedence ---

    @Test
    fun `multiplication before addition`() {
        // 2 + 3 * 4 = 2 + 12 = 14
        assertEqualsBD(BigDecimal("14"), eval("2 + 3 × 4"))
    }

    // --- Floating point precision with BigDecimal ---

    @Test
    fun `0_1 plus 0_2 equals 0_3`() {
        assertEqualsBD(BigDecimal("0.3"), eval("0.1 + 0.2"))
    }

    // --- Division by zero ---

    @Test
    fun `division by zero throws DivisionByZero`() {
        expectError("10 ÷ 0", CalculatorError.DivisionByZero::class.java)
    }

    @Test
    fun `zero divided by zero throws DivisionByZero`() {
        expectError("0 ÷ 0", CalculatorError.DivisionByZero::class.java)
    }

    // --- Non-terminating decimal ---

    @Test
    fun `10 divided by 3 uses defined scale without exception`() {
        val result = eval("10 ÷ 3")
        // Must not throw ArithmeticException; result rounded to 10 decimal places
        assertEqualsBD(BigDecimal("3.3333333333"), result)
    }

    // --- Percent operator ---

    @Test
    fun `5 percent equals 0_05`() {
        assertEqualsBD(BigDecimal("0.05"), eval("5 %"))
    }

    @Test
    fun `100 percent equals 1`() {
        assertEqualsBD(BigDecimal("1"), eval("100 %"))
    }

    @Test
    fun `percent in compound expression`() {
        // 200 × 10% → RPN: 200 10 % ×  →  200 × 0.1 = 20
        assertEqualsBD(BigDecimal("20"), eval("200 × 10 %"))
    }

    // --- Unary minus ---

    @Test
    fun `unary minus at start`() {
        assertEqualsBD(BigDecimal("2"), eval("-3 + 5"))
    }

    @Test
    fun `unary minus after operator`() {
        assertEqualsBD(BigDecimal("-6"), eval("2 × -3"))
    }

    @Test
    fun `unary minus inside parentheses`() {
        assertEqualsBD(BigDecimal("-2"), eval("(-2)"))
    }

    // --- Parentheses ---

    @Test
    fun `parentheses override precedence`() {
        assertEqualsBD(BigDecimal("20"), eval("(2 + 3) × 4"))
    }

    @Test
    fun `complex parentheses expression`() {
        // 2 × (3 + 4) ÷ 7 = 2 × 7 ÷ 7 = 2
        assertEqualsBD(BigDecimal("2"), eval("2 × (3 + 4) ÷ 7"))
    }

    // --- Incomplete / invalid expressions ---

    @Test
    fun `expression ending with operator is IncompleteExpression`() {
        expectError("2 +", CalculatorError.IncompleteExpression::class.java)
    }

    @Test
    fun `empty expression is IncompleteExpression`() {
        expectError("", CalculatorError.IncompleteExpression::class.java)
    }

    @Test
    fun `invalid character in expression is InvalidExpression`() {
        expectError("2 + abc", CalculatorError.InvalidExpression::class.java)
    }

    @Test
    fun `unclosed parenthesis is InvalidExpression`() {
        expectError("(2 + 3", CalculatorError.InvalidExpression::class.java)
    }

    // --- NumberTooLarge ---

    @Test
    fun `very large number multiplication throws NumberTooLarge`() {
        val huge = "9".repeat(300)
        expectError("$huge × $huge", CalculatorError.NumberTooLarge::class.java)
    }
}
