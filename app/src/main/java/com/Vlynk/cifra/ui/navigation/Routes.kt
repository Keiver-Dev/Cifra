package com.Vlynk.cifra.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes using Kotlinx Serialization
 * for Navigation Compose.
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

@Serializable
data object VaultSettings
