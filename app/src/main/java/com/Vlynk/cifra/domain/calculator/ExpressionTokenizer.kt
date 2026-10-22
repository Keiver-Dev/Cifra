package com.Vlynk.cifra.domain.calculator

import java.math.BigDecimal

/**
 * Tokenizer for arithmetic expressions.
 * Converts input strings into a sequence of Tokens.
 * Pure Kotlin without any Android framework dependencies.
 */
class ExpressionTokenizer {

    fun tokenize(expression: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var index = 0
        val length = expression.length

        while (index < length) {
            val char = expression[index]

            when {
                char.isWhitespace() -> {
                    index++
                }

                char.isDigit() || (char == '.' && index + 1 < length && expression[index + 1].isDigit()) -> {
                    val (numberToken, nextIndex) = readNumber(expression, index)
                    tokens.add(numberToken)
                    index = nextIndex
                }

                char == '-' || char == '−' -> {
                    val isUnary = tokens.isEmpty() || isPrecedingTokenOperatorOrLeftParen(tokens.last())
                    if (isUnary) {
                        // Lookahead for next non-whitespace character
                        var lookahead = index + 1
                        while (lookahead < length && expression[lookahead].isWhitespace()) {
                            lookahead++
                        }

                        if (lookahead < length && (expression[lookahead].isDigit() || expression[lookahead] == '.')) {
                            // Negative number
                            val (numberToken, nextIndex) = readNumber(expression, lookahead, isNegative = true)
                            tokens.add(numberToken)
                            index = nextIndex
                        } else if (lookahead < length && expression[lookahead] == '(') {
                            // -(...) equivalent to -1 * (...)
                            tokens.add(Token.Number(BigDecimal("-1")))
                            tokens.add(Token.Operator(Token.OperatorType.MULTIPLY))
                            index++
                        } else {
                            throw CalculatorError.InvalidExpression("Invalid unary minus placement at index $index")
                        }
                    } else {
                        tokens.add(Token.Operator(Token.OperatorType.SUBTRACT))
                        index++
                    }
                }

                char == '+' -> {
                    tokens.add(Token.Operator(Token.OperatorType.ADD))
                    index++
                }

                char == '*' || char == '×' || char == 'x' || char == 'X' -> {
                    tokens.add(Token.Operator(Token.OperatorType.MULTIPLY))
                    index++
                }

                char == '/' || char == '÷' -> {
                    tokens.add(Token.Operator(Token.OperatorType.DIVIDE))
                    index++
                }

                char == '%' -> {
                    tokens.add(Token.Operator(Token.OperatorType.PERCENT))
                    index++
                }

                char == '(' -> {
                    tokens.add(Token.LeftParen)
                    index++
                }

                char == ')' -> {
                    tokens.add(Token.RightParen)
                    index++
                }

                else -> {
                    throw CalculatorError.InvalidExpression("Invalid character '$char' at index $index")
                }
            }
        }

        return tokens
    }

    private fun readNumber(
        expression: String,
        startIndex: Int,
        isNegative: Boolean = false
    ): Pair<Token.Number, Int> {
        val sb = StringBuilder()
        if (isNegative) {
            sb.append('-')
        }

        var i = startIndex
        var hasDecimalPoint = false

        while (i < expression.length) {
            val ch = expression[i]
            if (ch.isDigit()) {
                sb.append(ch)
                i++
            } else if (ch == '.') {
                if (hasDecimalPoint) {
                    throw CalculatorError.InvalidExpression("Multiple decimal points found in number at index $i")
                }
                hasDecimalPoint = true
                sb.append(ch)
                i++
            } else {
                break
            }
        }

        val rawNumber = sb.toString()
        val normalized = if (rawNumber.startsWith(".")) {
            "0$rawNumber"
        } else if (rawNumber.startsWith("-.")) {
            "-0" + rawNumber.substring(2)
        } else {
            rawNumber
        }

        return try {
            val bd = BigDecimal(normalized)
            Pair(Token.Number(bd), i)
        } catch (_: NumberFormatException) {
            throw CalculatorError.InvalidExpression("Invalid number format '$rawNumber' at index $startIndex")
        }
    }

    private fun isPrecedingTokenOperatorOrLeftParen(token: Token): Boolean {
        return when (token) {
            is Token.Operator -> true
            is Token.LeftParen -> true
            is Token.Number, is Token.RightParen -> false
        }
    }
}
