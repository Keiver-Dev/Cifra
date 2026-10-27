package com.Vlynk.cifra.di

import android.content.Context
import com.Vlynk.cifra.data.repository.HistoryRepository
import com.Vlynk.cifra.domain.calculator.Evaluator
import com.Vlynk.cifra.domain.calculator.NumberFormatter
import com.Vlynk.cifra.domain.usecase.EvaluateExpressionUseCase

/**
 * Manual dependency injection container for the application.
 * Manages singleton instances of repositories, use cases, and app-level dependencies.
 */
class AppContainer(
    val context: Context
) {
    val historyRepository: HistoryRepository by lazy {
        HistoryRepository()
    }

    val evaluator: Evaluator by lazy {
        Evaluator()
    }

    val numberFormatter: NumberFormatter by lazy {
        NumberFormatter()
    }

    val evaluateExpressionUseCase: EvaluateExpressionUseCase by lazy {
        EvaluateExpressionUseCase(
            evaluator = evaluator,
            formatter = numberFormatter,
            historyRepository = historyRepository
        )
    }
}
