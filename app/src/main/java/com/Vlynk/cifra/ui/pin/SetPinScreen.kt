package com.Vlynk.cifra.ui.pin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.Vlynk.cifra.ui.theme.ColorAcidLime
import com.Vlynk.cifra.ui.theme.ColorAsh
import com.Vlynk.cifra.ui.theme.ColorBone
import com.Vlynk.cifra.ui.theme.ColorCarbon
import com.Vlynk.cifra.ui.theme.ColorCoralRed
import com.Vlynk.cifra.ui.theme.ColorFog
import com.Vlynk.cifra.ui.theme.ColorGraphite
import com.Vlynk.cifra.ui.theme.ColorMist
import com.Vlynk.cifra.ui.theme.ColorObsidian
import com.Vlynk.cifra.ui.theme.ColorSmoke
import com.Vlynk.cifra.ui.theme.ColorVoid

// ---------------------------------------------------------------------------
// Screen entry point
// ---------------------------------------------------------------------------

/**
 * Screen for first-time PIN setup (or PIN change from the vault settings).
 *
 * @param onSuccess   Called after the PIN is successfully saved. Navigate away here.
 * @param onNavigateBack   Called when the user presses Back/Cancel.
 * @param mode        [PinScreenMode.Setup] or [PinScreenMode.Change].
 */
@Composable
fun SetPinScreen(
    onSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    mode: PinScreenMode = PinScreenMode.Setup,
    viewModel: PinViewModel = viewModel(
        factory = if (mode == PinScreenMode.Setup) PinViewModel.SetupFactory
        else PinViewModel.ChangeFactory
    )
) {
    com.Vlynk.cifra.ui.vault.VaultPrivacyEffect()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Navigate away once saved
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onSuccess()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ColorVoid)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))

            // Lock icon built from shapes
            LockIcon()

            Spacer(Modifier.height(32.dp))

            // Title
            val title = when {
                mode == PinScreenMode.Change && uiState.isVerifyingCurrent -> "Verificar PIN actual"
                mode == PinScreenMode.Change -> "Nuevo PIN"
                else -> "Crear PIN del cofre"
            }
            Text(
                text = title,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = ColorBone,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            val subtitle = when {
                mode == PinScreenMode.Change && uiState.isVerifyingCurrent ->
                    "Introduce tu PIN actual para continuar."
                else ->
                    "Solo dígitos · entre 4 y 6 caracteres."
            }
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = ColorFog,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(48.dp))

            // Verification step (Change mode only)
            AnimatedVisibility(
                visible = mode == PinScreenMode.Change && uiState.isVerifyingCurrent,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    PinField(
                        label = "PIN actual",
                        value = uiState.currentPin,
                        onValueChange = viewModel::onCurrentPinChange,
                        error = uiState.currentPinError,
                        imeAction = ImeAction.Done,
                        onImeAction = viewModel::onVerifyCurrentPin
                    )
                    Spacer(Modifier.height(24.dp))
                    PinActionButton(
                        text = "Continuar",
                        enabled = !uiState.isLoading && uiState.currentPin.length >= 4,
                        isLoading = uiState.isLoading,
                        onClick = viewModel::onVerifyCurrentPin
                    )
                }
            }

            // New + Confirm step
            AnimatedVisibility(
                visible = !uiState.isVerifyingCurrent,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    PinField(
                        label = "Nuevo PIN",
                        value = uiState.newPin,
                        onValueChange = viewModel::onNewPinChange,
                        error = uiState.newPinError,
                        imeAction = ImeAction.Next
                    )

                    Spacer(Modifier.height(20.dp))

                    PinField(
                        label = "Confirmar PIN",
                        value = uiState.confirmPin,
                        onValueChange = viewModel::onConfirmPinChange,
                        error = uiState.confirmPinError,
                        imeAction = ImeAction.Done,
                        onImeAction = viewModel::onSave
                    )

                    Spacer(Modifier.height(8.dp))

                    // Global error (e.g. save failure)
                    AnimatedVisibility(visible = uiState.globalError != null) {
                        Text(
                            text = uiState.globalError ?: "",
                            color = ColorCoralRed,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(Modifier.height(32.dp))

                    PinActionButton(
                        text = if (mode == PinScreenMode.Setup) "Crear PIN" else "Cambiar PIN",
                        enabled = !uiState.isLoading &&
                                uiState.newPin.length >= 4 &&
                                uiState.confirmPin.length >= 4,
                        isLoading = uiState.isLoading,
                        onClick = viewModel::onSave
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Cancel / Back
            androidx.compose.material3.TextButton(onClick = onNavigateBack) {
                Text(
                    text = "Cancelar",
                    color = ColorAsh,
                    fontSize = 14.sp
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// PIN text field
// ---------------------------------------------------------------------------

@Composable
private fun PinField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: (() -> Unit)? = null
) {
    val focusRequester = remember { FocusRequester() }
    val hasError = error != null
    val borderColor = when {
        hasError -> ColorCoralRed
        value.isNotEmpty() -> ColorAcidLime
        else -> ColorSmoke
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (hasError) ColorCoralRed else ColorFog,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = ColorBone,
                letterSpacing = 8.sp,
                textAlign = TextAlign.Center
            ),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation('●'),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onDone = { onImeAction?.invoke() },
                onNext = { /* focus moves automatically */ }
            ),
            cursorBrush = SolidColor(ColorAcidLime),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .clip(RoundedCornerShape(12.dp))
                .background(ColorObsidian)
                .border(
                    width = if (hasError || value.isNotEmpty()) 1.5.dp else 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 20.dp, vertical = 18.dp),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.Center) {
                    if (value.isEmpty()) {
                        Text(
                            text = "● ● ● ●",
                            fontSize = 16.sp,
                            color = ColorAsh,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    innerTextField()
                }
            }
        )

        AnimatedVisibility(visible = hasError) {
            Text(
                text = error ?: "",
                color = ColorCoralRed,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 6.dp, start = 4.dp)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Action button
// ---------------------------------------------------------------------------

@Composable
private fun PinActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    val alpha by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.4f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "buttonAlpha"
    )

    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = ColorAcidLime,
            contentColor = ColorVoid,
            disabledContainerColor = ColorGraphite,
            disabledContentColor = ColorFog
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .alpha(alpha)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = ColorVoid,
                strokeWidth = 2.dp,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Decorative lock icon
// ---------------------------------------------------------------------------

@Composable
private fun LockIcon() {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(ColorGraphite)
            .border(1.dp, ColorSmoke, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // Inner rectangle (lock body)
        Box(
            modifier = Modifier
                .size(28.dp, 22.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(ColorAcidLime)
        )
        // Shackle arc suggestion (small circle cut-out visual — simplified)
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(ColorAcidLime)
                .align(Alignment.TopCenter)
                .padding(bottom = 6.dp)
        )
    }
}

// ---------------------------------------------------------------------------
// Preview
// ---------------------------------------------------------------------------

@Preview(showBackground = true, backgroundColor = 0xFF08090A, name = "SetPin - Setup")
@Composable
private fun SetPinScreenSetupPreview() {
    // Stateless preview — ViewModel injected at runtime
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorVoid),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            LockIcon()
            Spacer(Modifier.height(24.dp))
            Text("Crear PIN del cofre", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = ColorBone)
            Spacer(Modifier.height(8.dp))
            Text("Solo dígitos · entre 4 y 6 caracteres.", fontSize = 14.sp, color = ColorFog)
            Spacer(Modifier.height(40.dp))
            PinField(label = "Nuevo PIN", value = "", onValueChange = {})
            Spacer(Modifier.height(20.dp))
            PinField(label = "Confirmar PIN", value = "", onValueChange = {})
            Spacer(Modifier.height(32.dp))
            PinActionButton(text = "Crear PIN", onClick = {}, enabled = false)
        }
    }
}
