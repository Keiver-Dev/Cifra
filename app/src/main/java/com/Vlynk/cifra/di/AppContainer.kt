package com.Vlynk.cifra.di

import android.content.Context

/**
 * Manual dependency injection container for the application.
 * Will be populated in each phase with Room database,
 * DataStore preferences, repositories, and use cases.
 */
class AppContainer(
    val context: Context
) {
    // Dependencies to be initialized in upcoming phases
}
