package com.Vlynk.cifra.ui.vault

import android.app.Activity
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

/**
 * Applies FLAG_SECURE to the host Activity Window while the composable is active.
 *
 * This prevents:
 * 1. Screenshots and screen recordings of private vault notes.
 * 2. Visual previews of secret notes in Android's Recent Apps (app switcher).
 *
 * When disposed (e.g. user exits back to the Calculator), the flag is cleared.
 */
@Composable
fun VaultPrivacyEffect() {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}
