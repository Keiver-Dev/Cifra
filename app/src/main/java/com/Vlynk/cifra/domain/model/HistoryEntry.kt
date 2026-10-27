package com.Vlynk.cifra.domain.model

/**
 * Represents an entry in the calculation history.
 *
 * @property expression The mathematical expression that was calculated.
 * @property result The formatted result string of the evaluation.
 * @property createdAt Timestamp in milliseconds when the calculation occurred.
 */
data class HistoryEntry(
    val expression: String,
    val result: String,
    val createdAt: Long = System.currentTimeMillis()
)
