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

@Composable
fun CifraNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Calculator,
        modifier = modifier
    ) {
        composable<Calculator> {
            PlaceholderCalculatorScreen(
                onNavigateToVault = { navController.navigate(VaultHome) },
                onNavigateToNoteEditor = { navController.navigate(NoteEditor(null)) },
                onNavigateToSetPin = { navController.navigate(SetPin) },
                onNavigateToTools = { navController.navigate(Tools) }
            )
        }

        composable<VaultHome> {
            PlaceholderDestinationScreen(
                title = "Secret Vault (VaultHome)",
                onBack = { navController.popBackStack() }
            )
        }

        composable<NoteEditor> { backStackEntry ->
            val route = backStackEntry.toRoute<NoteEditor>()
            PlaceholderDestinationScreen(
                title = "Note Editor (NoteEditor) - ID: ${route.noteId ?: "New"}",
                onBack = { navController.popBackStack() }
            )
        }

        composable<SetPin> {
            PlaceholderDestinationScreen(
                title = "Set PIN (SetPin)",
                onBack = { navController.popBackStack() }
            )
        }

        composable<Tools> {
            PlaceholderDestinationScreen(
                title = "Tools (Tools)",
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
