package com.Vlynk.cifra.di

import android.content.Context
import androidx.room.Room
import com.Vlynk.cifra.data.local.AppDatabase
import com.Vlynk.cifra.data.repository.HistoryRepository
import com.Vlynk.cifra.data.repository.NoteRepository
import com.Vlynk.cifra.data.repository.NoteRepositoryImpl
import com.Vlynk.cifra.domain.calculator.Evaluator
import com.Vlynk.cifra.domain.calculator.NumberFormatter
import com.Vlynk.cifra.domain.usecase.DeleteNoteUseCase
import com.Vlynk.cifra.domain.usecase.EvaluateExpressionUseCase
import com.Vlynk.cifra.domain.usecase.ObserveNotesUseCase
import com.Vlynk.cifra.domain.usecase.SaveNoteUseCase

/**
 * Manual dependency injection container for the application.
 * Manages singleton instances of databases, repositories, use cases, and app-level dependencies.
 */
class AppContainer(
    val context: Context
) {
    // --- Room Database ---
    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "cifra_vault.db"
        ).build()
    }

    // --- Repositories ---
    val historyRepository: HistoryRepository by lazy {
        HistoryRepository()
    }

    val noteRepository: NoteRepository by lazy {
        NoteRepositoryImpl(database.noteDao())
    }

    // --- Calculator Engine ---
    val evaluator: Evaluator by lazy {
        Evaluator()
    }

    val numberFormatter: NumberFormatter by lazy {
        NumberFormatter()
    }

    // --- Calculator Use Cases ---
    val evaluateExpressionUseCase: EvaluateExpressionUseCase by lazy {
        EvaluateExpressionUseCase(
            evaluator = evaluator,
            formatter = numberFormatter,
            historyRepository = historyRepository
        )
    }

    // --- Vault Use Cases ---
    val observeNotesUseCase: ObserveNotesUseCase by lazy {
        ObserveNotesUseCase(noteRepository)
    }

    val saveNoteUseCase: SaveNoteUseCase by lazy {
        SaveNoteUseCase(noteRepository)
    }

    val deleteNoteUseCase: DeleteNoteUseCase by lazy {
        DeleteNoteUseCase(noteRepository)
    }
}
