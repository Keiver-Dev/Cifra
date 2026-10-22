package com.Vlynk.cifra.domain.calculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class ExpressionTokenizerTest {

    private lateinit var tokenizer: ExpressionTokenizer

    @Before
    fun setUp() {
        tokenizer = ExpressionTokenizer()
    }

    @Test
    fun `tokenize empty or blank string returns empty list`() {
        assertTrue(tokenizer.tokenize("").isEmpty())
        assertTrue(tokenizer.tokenize("   ").isEmpty())
    }

    @Test
    fun `tokenize integer numbers`() {
        val tokens = tokenizer.tokenize("123")
        assertEquals(1, tokens.size)
        assertEquals(Token.Number(BigDecimal("123")), tokens[0])
    }

    @Test
    fun `tokenize numbers with decimals and leading dot`() {
        val tokens = tokenizer.tokenize("3.14 + .5")
        assertEquals(3, tokens.size)
        assertEquals(Token.Number(BigDecimal("3.14")), tokens[0])
        assertEquals(Token.Operator(Token.OperatorType.ADD), tokens[1])
        assertEquals(Token.Number(BigDecimal("0.5")), tokens[2])
    }

    @Test
    fun `tokenize basic operators with whitespace`() {
        val tokens = tokenizer.tokenize("10 + 20 − 5 × 2 ÷ 4 %")
        val expected = listOf(
            Token.Number(BigDecimal("10")),
            Token.Operator(Token.OperatorType.ADD),
            Token.Number(BigDecimal("20")),
            Token.Operator(Token.OperatorType.SUBTRACT),
            Token.Number(BigDecimal("5")),
            Token.Operator(Token.OperatorType.MULTIPLY),
            Token.Number(BigDecimal("2")),
            Token.Operator(Token.OperatorType.DIVIDE),
            Token.Number(BigDecimal("4")),
            Token.Operator(Token.OperatorType.PERCENT)
        )
        assertEquals(expected, tokens)
    }

    @Test
    fun `tokenize supports ascii operator aliases`() {
        val tokens = tokenizer.tokenize("5 - 2 * 3 / 1")
        val expected = listOf(
            Token.Number(BigDecimal("5")),
            Token.Operator(Token.OperatorType.SUBTRACT),
            Token.Number(BigDecimal("2")),
            Token.Operator(Token.OperatorType.MULTIPLY),
            Token.Number(BigDecimal("3")),
            Token.Operator(Token.OperatorType.DIVIDE),
            Token.Number(BigDecimal("1"))
        )
        assertEquals(expected, tokens)
    }

    @Test
    fun `tokenize parentheses`() {
        val tokens = tokenizer.tokenize("(2 + 3) * 4")
        val expected = listOf(
            Token.LeftParen,
            Token.Number(BigDecimal("2")),
            Token.Operator(Token.OperatorType.ADD),
            Token.Number(BigDecimal("3")),
            Token.RightParen,
            Token.Operator(Token.OperatorType.MULTIPLY),
            Token.Number(BigDecimal("4"))
        )
        assertEquals(expected, tokens)
    }

    @Test
    fun `tokenize unary minus at start of expression`() {
        val tokens = tokenizer.tokenize("-3 + 5")
        val expected = listOf(
            Token.Number(BigDecimal("-3")),
            Token.Operator(Token.OperatorType.ADD),
            Token.Number(BigDecimal("5"))
        )
        assertEquals(expected, tokens)
    }

    @Test
    fun `tokenize unary minus after operator`() {
        val tokens = tokenizer.tokenize("2 × -3")
        val expected = listOf(
            Token.Number(BigDecimal("2")),
            Token.Operator(Token.OperatorType.MULTIPLY),
            Token.Number(BigDecimal("-3"))
        )
        assertEquals(expected, tokens)
    }

    @Test
    fun `tokenize unary minus inside parentheses`() {
        val tokens = tokenizer.tokenize("(-2)")
        val expected = listOf(
            Token.LeftParen,
            Token.Number(BigDecimal("-2")),
            Token.RightParen
        )
        assertEquals(expected, tokens)
    }

    @Test
    fun `tokenize unary minus preceding opening parenthesis`() {
        val tokens = tokenizer.tokenize("-(2 + 3)")
        val expected = listOf(
            Token.Number(BigDecimal("-1")),
            Token.Operator(Token.OperatorType.MULTIPLY),
            Token.LeftParen,
            Token.Number(BigDecimal("2")),
            Token.Operator(Token.OperatorType.ADD),
            Token.Number(BigDecimal("3")),
            Token.RightParen
        )
        assertEquals(expected, tokens)
    }

    @Test
    fun `tokenize binary minus between numbers`() {
        val tokens = tokenizer.tokenize("5 - 3")
        val expected = listOf(
            Token.Number(BigDecimal("5")),
            Token.Operator(Token.OperatorType.SUBTRACT),
            Token.Number(BigDecimal("3"))
        )
        assertEquals(expected, tokens)
    }

    @Test
    fun `reject invalid characters with InvalidExpression`() {
        assertThrows(CalculatorError.InvalidExpression::class.java) {
            tokenizer.tokenize("2 + abc")
        }
        assertThrows(CalculatorError.InvalidExpression::class.java) {
            tokenizer.tokenize("5 @ 2")
        }
        assertThrows(CalculatorError.InvalidExpression::class.java) {
            tokenizer.tokenize("3 # 4")
        }
    }

    @Test
    fun `reject multiple decimal points with InvalidExpression`() {
        assertThrows(CalculatorError.InvalidExpression::class.java) {
            tokenizer.tokenize("1.2.3")
        }
        assertThrows(CalculatorError.InvalidExpression::class.java) {
            tokenizer.tokenize("1..2")
        }
        assertThrows(CalculatorError.InvalidExpression::class.java) {
            tokenizer.tokenize("..")
        }
    }
}
