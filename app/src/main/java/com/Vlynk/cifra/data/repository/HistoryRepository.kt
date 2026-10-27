package com.Vlynk.cifra.data.repository

import com.Vlynk.cifra.domain.model.HistoryEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory repository for calculation history.
 * Retains a maximum of 50 entries, ordered newest first.
 */
class HistoryRepository {

    companion object {
        const val MAX_HISTORY_SIZE = 50
    }

    private val _history = MutableStateFlow<List<HistoryEntry>>(emptyList())
    val history: StateFlow<List<HistoryEntry>> = _history.asStateFlow()

    /**
     * Adds a new history entry at the head of the list, keeping at most [MAX_HISTORY_SIZE] entries.
     */
    fun addEntry(entry: HistoryEntry) {
        val updated = listOf(entry) + _history.value
        _history.value = updated.take(MAX_HISTORY_SIZE)
    }

    /**
     * Clears all calculation history entries.
     */
    fun clear() {
        _history.value = emptyList()
    }
}
