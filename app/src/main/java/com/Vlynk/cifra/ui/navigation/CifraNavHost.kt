package com.Vlynk.cifra.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.Vlynk.cifra.R
import com.Vlynk.cifra.ui.theme.ColorAcidLime
import com.Vlynk.cifra.ui.theme.ColorBone
import com.Vlynk.cifra.ui.theme.ColorCarbon
import com.Vlynk.cifra.ui.theme.ColorFog
import com.Vlynk.cifra.ui.theme.ColorVoid
import com.Vlynk.cifra.ui.pin.PinScreenMode
import com.Vlynk.cifra.ui.pin.SetPinScreen

import androidx.navigation.NavDestination.Companion.hasRoute

@Composable
fun CifraNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    // Security & privacy (6.1): FLAG_SECURE while vault screens are open, cleared on Calculator
    androidx.compose.runtime.DisposableEffect(navController) {
        val listener = androidx.navigation.NavController.OnDestinationChangedListener { _, destination, _ ->
            val isVaultActive = !destination.hasRoute<Calculator>()
            val window = (context as? android.app.Activity)?.window
            if (isVaultActive) {
                window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
            } else {
                window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
            }
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose {
            navController.removeOnDestinationChangedListener(listener)
            (context as? android.app.Activity)?.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    // Stealth (5.4): close the vault automatically when the app transitions to the background
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner, navController) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_STOP) {
                val currentDestination = navController.currentBackStackEntry?.destination
                if (currentDestination != null && !currentDestination.hasRoute<Calculator>()) {
                    navController.popBackStack(Calculator, inclusive = false)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    NavHost(
        navController = navController,
        startDestination = Calculator,
        modifier = modifier
    ) {
        composable<Calculator> {
            com.Vlynk.cifra.ui.calculator.CalculatorScreen(
                onNavigateToVault = { navController.navigate(VaultHome) },
                onNavigateToSetPin = { navController.navigate(SetPin) }
            )
        }

        composable<VaultHome>(
            enterTransition = {
                fadeIn(animationSpec = tween(350)) + slideInVertically(
                    animationSpec = tween(350),
                    initialOffsetY = { it / 12 }
                )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(250))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(250))
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(250)) + slideOutVertically(
                    animationSpec = tween(250),
                    targetOffsetY = { it / 12 }
                )
            }
        ) {
            com.Vlynk.cifra.ui.vault.VaultHomeScreen(
                onNoteClick = { noteId -> navController.navigate(NoteEditor(noteId)) },
                onNavigateToCreate = { navController.navigate(NoteEditor(null)) },
                onNavigateToSettings = { navController.navigate(VaultSettings) },
                onNavigateToTools = { navController.navigate(Tools) },
                onClose = { navController.popBackStack(Calculator, inclusive = false) }
            )
        }

        composable<NoteEditor> { backStackEntry ->
            val route = backStackEntry.toRoute<NoteEditor>()
            com.Vlynk.cifra.ui.vault.NoteEditorScreen(
                noteId = route.noteId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<SetPin> {
            SetPinScreen(
                mode = PinScreenMode.Setup,
                onSuccess = {
                    // First time: PIN created, go back to calculator
                    navController.popBackStack(Calculator, inclusive = false)
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<ChangePin> {
            SetPinScreen(
                mode = PinScreenMode.Change,
                onSuccess = { navController.popBackStack() },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Tools> {
            PlaceholderDestinationScreen(
                title = stringResource(R.string.tools_title),
                subtitle = stringResource(R.string.tools_coming_soon),
                onBack = { navController.popBackStack() }
            )
        }

        composable<VaultSettings> {
            VaultSettingsPlaceholderScreen(
                onChangePin = { navController.navigate(ChangePin) },
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
private fun PlaceholderDestinationScreen(
    title: String,
    subtitle: String = "",
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorVoid)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = ColorBone
        )

        if (subtitle.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = ColorFog
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onBack,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ColorAcidLime,
                contentColor = ColorVoid
            )
        ) {
            Text(
                text = stringResource(R.string.vault_settings_back),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun VaultSettingsPlaceholderScreen(
    onChangePin: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorVoid)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.vault_settings_title),
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = ColorBone
        )

        Spacer(modifier = Modifier.height(36.dp))

        Button(
            onClick = onChangePin,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ColorAcidLime,
                contentColor = ColorVoid
            )
        ) {
            Text(
                text = stringResource(R.string.vault_settings_change_pin),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = ColorBone
            )
        ) {
            Text(
                text = stringResource(R.string.vault_settings_back),
                fontSize = 15.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}