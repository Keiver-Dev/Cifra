package com.Vlynk.cifra.domain.calculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class ExpressionParserTest {

    private lateinit var parser: ExpressionParser
    private lateinit var tokenizer: ExpressionTokenizer

    @Before
    fun setUp() {
        parser = ExpressionParser()
        tokenizer = ExpressionTokenizer()
    }

    private fun parseInfix(expression: String): List<Token> {
        val tokens = tokenizer.tokenize(expression)
        return parser.parseToRpn(tokens)
    }

    @Test
    fun `parse simple addition to RPN`() {
        val rpn = parseInfix("2 + 3")
        val expected = listOf(
            Token.Number(BigDecimal("2")),
            Token.Number(BigDecimal("3")),
            Token.Operator(Token.OperatorType.ADD)
        )
        assertEquals(expected, rpn)
    }

    @Test
    fun `operator precedence multiplication over addition`() {
        val rpn = parseInfix("2 + 3 * 4")
        val expected = listOf(
            Token.Number(BigDecimal("2")),
            Token.Number(BigDecimal("3")),
            Token.Number(BigDecimal("4")),
            Token.Operator(Token.OperatorType.MULTIPLY),
            Token.Operator(Token.OperatorType.ADD)
        )
        assertEquals(expected, rpn)
    }

    @Test
    fun `left associativity for same precedence operators`() {
        val rpn = parseInfix("10 - 4 - 2")
        val expected = listOf(
            Token.Number(BigDecimal("10")),
            Token.Number(BigDecimal("4")),
            Token.Operator(Token.OperatorType.SUBTRACT),
            Token.Number(BigDecimal("2")),
            Token.Operator(Token.OperatorType.SUBTRACT)
        )
        assertEquals(expected, rpn)
    }

    @Test
    fun `parentheses override precedence`() {
        val rpn = parseInfix("(2 + 3) * 4")
        val expected = listOf(
            Token.Number(BigDecimal("2")),
            Token.Number(BigDecimal("3")),
            Token.Operator(Token.OperatorType.ADD),
            Token.Number(BigDecimal("4")),
            Token.Operator(Token.OperatorType.MULTIPLY)
        )
        assertEquals(expected, rpn)
    }

    @Test
    fun `nested parentheses`() {
        val rpn = parseInfix("2 * ((3 + 4) - 1)")
        val expected = listOf(
            Token.Number(BigDecimal("2")),
            Token.Number(BigDecimal("3")),
            Token.Number(BigDecimal("4")),
            Token.Operator(Token.OperatorType.ADD),
            Token.Number(BigDecimal("1")),
            Token.Operator(Token.OperatorType.SUBTRACT),
            Token.Operator(Token.OperatorType.MULTIPLY)
        )
        assertEquals(expected, rpn)
    }

    @Test
    fun `postfix percent operator`() {
        val rpn = parseInfix("5 %")
        val expected = listOf(
            Token.Number(BigDecimal("5")),
            Token.Operator(Token.OperatorType.PERCENT)
        )
        assertEquals(expected, rpn)
    }

    @Test
    fun `reject unclosed parenthesis with InvalidExpression`() {
        assertThrows(CalculatorError.InvalidExpression::class.java) {
            parseInfix("(2 + 3")
        }
        assertThrows(CalculatorError.InvalidExpression::class.java) {
            parseInfix("2 * (3 + (4 - 1)")
        }
    }

    @Test
    fun `reject unopened parenthesis with InvalidExpression`() {
        assertThrows(CalculatorError.InvalidExpression::class.java) {
            parseInfix("2 + 3)")
        }
        assertThrows(CalculatorError.InvalidExpression::class.java) {
            parseInfix(")2 + 3(")
        }
    }

    @Test
    fun `reject empty expression with IncompleteExpression`() {
        assertThrows(CalculatorError.IncompleteExpression::class.java) {
            parseInfix("")
        }
    }

    @Test
    fun `reject expression ending with binary operator with IncompleteExpression`() {
        assertThrows(CalculatorError.IncompleteExpression::class.java) {
            parseInfix("2 +")
        }
        assertThrows(CalculatorError.IncompleteExpression::class.java) {
            parseInfix("10 *")
        }
        assertThrows(CalculatorError.IncompleteExpression::class.java) {
            parseInfix("5 -")
        }
    }

    @Test
    fun `reject consecutive binary operators with InvalidExpression`() {
        assertThrows(CalculatorError.InvalidExpression::class.java) {
            parseInfix("2 + * 3")
        }
    }

    @Test
    fun `reject empty parentheses with InvalidExpression`() {
        assertThrows(CalculatorError.InvalidExpression::class.java) {
            parseInfix("2 + ()")
        }
    }
}
