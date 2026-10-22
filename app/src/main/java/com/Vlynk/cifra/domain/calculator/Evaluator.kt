package com.Vlynk.cifra.domain.calculator

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Evaluates an RPN (postfix) token list using BigDecimal arithmetic.
 * Pure Kotlin without any Android framework dependencies.
 */
class Evaluator {

    companion object {
        // Division scale: number of decimal places for non-terminating results
        private const val DIVISION_SCALE = 10
        private val DIVISION_ROUNDING = RoundingMode.HALF_UP
        // Max digits allowed in a result to catch NumberTooLarge
        private const val MAX_DIGITS = 300
    }

    private val tokenizer = ExpressionTokenizer()
    private val parser = ExpressionParser()

    /**
     * Entry point: parses and evaluates an arithmetic expression string.
     * @return Result<BigDecimal> — success with the result, or failure with a CalculatorError.
     */
    fun evaluate(expression: String): Result<BigDecimal> {
        return runCatching {
            val tokens = tokenizer.tokenize(expression)
            val rpn = parser.parseToRpn(tokens)
            evaluateRpn(rpn)
        }.mapFailure()
    }

    private fun evaluateRpn(rpn: List<Token>): BigDecimal {
        val stack = ArrayDeque<BigDecimal>()

        for (token in rpn) {
            when (token) {
                is Token.Number -> stack.addLast(token.value)

                is Token.Operator -> {
                    when (token.type) {
                        Token.OperatorType.ADD -> {
                            val (a, b) = popTwo(stack)
                            stack.addLast(checkSize(a + b))
                        }
                        Token.OperatorType.SUBTRACT -> {
                            val (a, b) = popTwo(stack)
                            stack.addLast(checkSize(a - b))
                        }
                        Token.OperatorType.MULTIPLY -> {
                            val (a, b) = popTwo(stack)
                            stack.addLast(checkSize(a * b))
                        }
                        Token.OperatorType.DIVIDE -> {
                            val (a, b) = popTwo(stack)
                            if (b.compareTo(BigDecimal.ZERO) == 0) {
                                throw CalculatorError.DivisionByZero()
                            }
                            val result = a.divide(b, DIVISION_SCALE, DIVISION_ROUNDING)
                                .stripTrailingZeros()
                            stack.addLast(checkSize(result))
                        }
                        Token.OperatorType.PERCENT -> {
                            val a = popOne(stack)
                            stack.addLast(a.divide(BigDecimal("100"), DIVISION_SCALE, DIVISION_ROUNDING).stripTrailingZeros())
                        }
                    }
                }

                is Token.LeftParen, is Token.RightParen -> {
                    // Parens should never appear in a valid RPN sequence
                    throw CalculatorError.InvalidExpression("Unexpected parenthesis in RPN output")
                }
            }
        }

        if (stack.size != 1) {
            throw CalculatorError.IncompleteExpression("Malformed expression: leftover values in stack")
        }

        return stack.last()
    }

    private fun popTwo(stack: ArrayDeque<BigDecimal>): Pair<BigDecimal, BigDecimal> {
        if (stack.size < 2) throw CalculatorError.IncompleteExpression("Not enough operands")
        val b = stack.removeLast()
        val a = stack.removeLast()
        return Pair(a, b)
    }

    private fun popOne(stack: ArrayDeque<BigDecimal>): BigDecimal {
        if (stack.isEmpty()) throw CalculatorError.IncompleteExpression("Not enough operands")
        return stack.removeLast()
    }

    private fun checkSize(value: BigDecimal): BigDecimal {
        val plain = value.toPlainString().replace("-", "").replace(".", "")
        if (plain.length > MAX_DIGITS) {
            throw CalculatorError.NumberTooLarge("Result exceeds maximum allowed digits ($MAX_DIGITS)")
        }
        return value
    }

    private fun Result<BigDecimal>.mapFailure(): Result<BigDecimal> {
        val ex = exceptionOrNull() ?: return this
        if (ex is CalculatorError) return this
        return Result.failure(CalculatorError.InvalidExpression(ex.message ?: "Unknown error"))
    }
}
