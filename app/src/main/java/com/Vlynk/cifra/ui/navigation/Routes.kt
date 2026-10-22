package com.Vlynk.cifra.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Destinos de navegación tipados usando Kotlinx Serialization
 * para Navigation Compose.
 */

@Serializable
data object Calculator

@Serializable
data object VaultHome

@Serializable
data class NoteEditor(val noteId: Long? = null)

@Serializable
data object SetPin

@Serializable
data object Tools
