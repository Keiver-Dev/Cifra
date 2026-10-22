package com.Vlynk.cifra.domain.calculator

/**
 * Parses infix arithmetic tokens into Reverse Polish Notation (RPN)
 * using Dijkstra's Shunting-yard algorithm.
 * Pure Kotlin without any Android framework dependencies.
 */
class ExpressionParser {

    /**
     * Validates and converts a list of infix tokens to RPN (postfix).
     * @throws CalculatorError.IncompleteExpression if expression is empty or ends prematurely
     * @throws CalculatorError.InvalidExpression if syntax is invalid or parentheses mismatched
     */
    fun parseToRpn(tokens: List<Token>): List<Token> {
        if (tokens.isEmpty()) {
            throw CalculatorError.IncompleteExpression("Expression is empty")
        }

        validateTokenSequence(tokens)

        val outputQueue = mutableListOf<Token>()
        val operatorStack = ArrayDeque<Token>()

        for (token in tokens) {
            when (token) {
                is Token.Number -> {
                    outputQueue.add(token)
                }

                is Token.Operator -> {
                    if (token.type == Token.OperatorType.PERCENT) {
                        // Postfix unary operator applies immediately in RPN
                        outputQueue.add(token)
                    } else {
                        // Left-associative binary operator: pop operators with >= precedence
                        while (operatorStack.isNotEmpty()) {
                            val top = operatorStack.last()
                            if (top is Token.Operator && top.type.precedence >= token.type.precedence) {
                                outputQueue.add(operatorStack.removeLast())
                            } else {
                                break
                            }
                        }
                        operatorStack.addLast(token)
                    }
                }

                is Token.LeftParen -> {
                    operatorStack.addLast(token)
                }

                is Token.RightParen -> {
                    var matchedLeftParen = false
                    while (operatorStack.isNotEmpty()) {
                        val top = operatorStack.removeLast()
                        if (top is Token.LeftParen) {
                            matchedLeftParen = true
                            break
                        } else {
                            outputQueue.add(top)
                        }
                    }
                    if (!matchedLeftParen) {
                        throw CalculatorError.InvalidExpression("Mismatched closing parenthesis without open parenthesis")
                    }
                }
            }
        }

        while (operatorStack.isNotEmpty()) {
            val top = operatorStack.removeLast()
            if (top is Token.LeftParen) {
                throw CalculatorError.InvalidExpression("Unclosed opening parenthesis")
            }
            outputQueue.add(top)
        }

        return outputQueue
    }

    private fun validateTokenSequence(tokens: List<Token>) {
        val firstToken = tokens.first()
        if (firstToken is Token.Operator) {
            throw CalculatorError.InvalidExpression("Expression cannot start with operator: ${firstToken.type.symbol}")
        }
        if (firstToken is Token.RightParen) {
            throw CalculatorError.InvalidExpression("Expression cannot start with a closing parenthesis")
        }

        val lastToken = tokens.last()
        if (lastToken is Token.Operator && lastToken.type != Token.OperatorType.PERCENT) {
            throw CalculatorError.IncompleteExpression("Expression ends with an operator: ${lastToken.type.symbol}")
        }

        for (i in 0 until tokens.size - 1) {
            val current = tokens[i]
            val next = tokens[i + 1]

            when (current) {
                is Token.Operator -> {
                    if (current.type != Token.OperatorType.PERCENT && next is Token.Operator) {
                        throw CalculatorError.InvalidExpression("Consecutive operators: ${current.type.symbol} ${next.type.symbol}")
                    }
                    if (current.type != Token.OperatorType.PERCENT && next is Token.RightParen) {
                        throw CalculatorError.InvalidExpression("Operator ${current.type.symbol} cannot be immediately followed by closing parenthesis")
                    }
                }

                is Token.LeftParen -> {
                    if (next is Token.RightParen) {
                        throw CalculatorError.InvalidExpression("Empty parentheses '()' are not allowed")
                    }
                    if (next is Token.Operator && next.type != Token.OperatorType.PERCENT) {
                        throw CalculatorError.InvalidExpression("Opening parenthesis cannot be immediately followed by operator ${next.type.symbol}")
                    }
                }

                is Token.Number -> {
                    if (next is Token.Number || next is Token.LeftParen) {
                        throw CalculatorError.InvalidExpression("Missing operator between terms")
                    }
                }

                is Token.RightParen -> {
                    if (next is Token.Number || next is Token.LeftParen) {
                        throw CalculatorError.InvalidExpression("Missing operator after closing parenthesis")
                    }
                }
            }
        }
    }
}
