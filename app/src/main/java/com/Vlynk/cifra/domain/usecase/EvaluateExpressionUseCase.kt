package com.Vlynk.cifra.domain.usecase

import com.Vlynk.cifra.data.repository.HistoryRepository
import com.Vlynk.cifra.domain.calculator.Evaluator
import com.Vlynk.cifra.domain.calculator.NumberFormatter
import com.Vlynk.cifra.domain.model.HistoryEntry

/**
 * Evaluates an infix expression, formats the resulting BigDecimal,
 * and records the entry in [HistoryRepository] if evaluation was successful.
 */
class EvaluateExpressionUseCase(
    private val evaluator: Evaluator = Evaluator(),
    private val formatter: NumberFormatter = NumberFormatter(),
    private val historyRepository: HistoryRepository
) {
    /**
     * Executes evaluation of the given [expression].
     * @param saveToHistory whether to record this evaluation in [historyRepository].
     *                      Defaults to true; pass false for stealth operations like failed PIN attempts.
     * @return [Result.success] with the formatted string on success,
     *         or [Result.failure] with [CalculatorError] on failure.
     */
    operator fun invoke(expression: String, saveToHistory: Boolean = true): Result<String> {
        val evalResult = evaluator.evaluate(expression)
        return evalResult.fold(
            onSuccess = { bigDecimal ->
                val formatted = formatter.format(bigDecimal)
                if (saveToHistory) {
                    historyRepository.addEntry(
                        HistoryEntry(
                            expression = expression,
                            result = formatted
                        )
                    )
                }
                Result.success(formatted)
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }
}
