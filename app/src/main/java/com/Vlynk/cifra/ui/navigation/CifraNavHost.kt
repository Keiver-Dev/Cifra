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
import androidx.compose.ui.tooling.preview.Preview
import com.Vlynk.cifra.ui.pin.PinScreenMode
import com.Vlynk.cifra.ui.pin.SetPinScreen

import androidx.navigation.NavDestination.Companion.hasRoute

@Composable
fun CifraNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    // Stealth: close the vault automatically when the app transitions to the background
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

        composable<VaultHome> {
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
                title = "Tools (Tools)",
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
private fun PlaceholderCalculatorScreen(
    onNavigateToVault: () -> Unit,
    onNavigateToNoteEditor: () -> Unit,
    onNavigateToSetPin: () -> Unit,
    onNavigateToTools: () -> Unit
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Cifra",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Calculator (Home)",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )

            Text(
                text = "Phase 1 test navigation:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onNavigateToVault,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Go to Secret Vault (VaultHome)")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onNavigateToNoteEditor,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Go to Note Editor (NoteEditor)")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onNavigateToSetPin,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Go to Set PIN (SetPin)")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onNavigateToTools,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Go to Tools (Tools)")
            }
        }
    }
}

@Composable
private fun PlaceholderDestinationScreen(
    title: String,
    onBack: () -> Unit
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(onClick = onBack) {
                Text("Back to Calculator")
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Calculator Screen Preview")
@Composable
private fun PlaceholderCalculatorScreenPreview() {
    MaterialTheme {
        PlaceholderCalculatorScreen(
            onNavigateToVault = {},
            onNavigateToNoteEditor = {},
            onNavigateToSetPin = {},
            onNavigateToTools = {}
        )
    }
}

@Composable
private fun VaultSettingsPlaceholderScreen(
    onChangePin: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Ajustes del cofre",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onChangePin,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cambiar PIN")
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Volver")
            }
        }
    }
}