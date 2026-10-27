package com.Vlynk.cifra.ui.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.Vlynk.cifra.R
import com.Vlynk.cifra.ui.calculator.components.Display
import com.Vlynk.cifra.ui.calculator.components.Keypad
import com.Vlynk.cifra.ui.theme.ColorBone
import com.Vlynk.cifra.ui.theme.ColorObsidian
import com.Vlynk.cifra.ui.theme.ColorPaper
import com.Vlynk.cifra.ui.theme.ColorVoid

/**
 * Main Calculator screen displaying formula Display, 4x5 Keypad, and History bottom sheet trigger.
 * Handles both portrait and landscape screen sizes responsively.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    modifier: Modifier = Modifier,
    viewModel: CalculatorViewModel = viewModel(factory = CalculatorViewModel.Factory),
    onNavigateToVault: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showHistorySheet by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ColorVoid)
            .statusBarsPadding()
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isLandscape = maxWidth > maxHeight

            if (isLandscape) {
                // Landscape orientation: side-by-side layout
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        TopBar(
                            onHistoryClick = { showHistorySheet = true },
                            onVaultClick = onNavigateToVault
                        )
                        Display(
                            expression = state.expression,
                            result = state.result,
                            error = state.error,
                            errorRes = state.errorRes,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Keypad(
                        onAction = viewModel::onAction,
                        modifier = Modifier
                            .weight(1.3f)
                            .fillMaxHeight()
                            .padding(start = 8.dp)
                    )
                }
            } else {
                // Portrait orientation: vertical layout with Display on top, Keypad below
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    TopBar(
                        onHistoryClick = { showHistorySheet = true },
                        onVaultClick = onNavigateToVault
                    )

                    Display(
                        expression = state.expression,
                        result = state.result,
                        error = state.error,
                        errorRes = state.errorRes,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )

                    Keypad(
                        onAction = viewModel::onAction,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1.6f)
                            .padding(bottom = 8.dp)
                    )
                }
            }
        }

        if (showHistorySheet) {
            HistorySheet(
                history = state.history,
                onReuseResult = { reused ->
                    viewModel.onAction(CalculatorAction.ReuseResult(reused))
                },
                onClearAll = {
                    viewModel.onAction(CalculatorAction.ClearHistory)
                },
                onDismissRequest = { showHistorySheet = false }
            )
        }
    }
}

@Composable
private fun TopBar(
    onHistoryClick: () -> Unit,
    onVaultClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.calculator_title),
            color = ColorPaper,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { onVaultClick() }
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // History button
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = ColorObsidian,
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onHistoryClick() }
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    HistoryIcon(
                        tint = ColorBone,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Vault temporary access button (until Phase 5 PIN trigger)
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = ColorObsidian,
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onVaultClick() }
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    VaultTriggerIcon(
                        tint = com.Vlynk.cifra.ui.theme.ColorAcidLime,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryIcon(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 1.8.dp.toPx()
        val radius = size.minDimension / 2 - strokeWidth
        val center = Offset(size.width / 2, size.height / 2)

        drawCircle(
            color = tint,
            radius = radius,
            center = center,
            style = Stroke(width = strokeWidth)
        )
        drawLine(
            color = tint,
            start = center,
            end = Offset(center.x, center.y - radius * 0.55f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = center,
            end = Offset(center.x + radius * 0.45f, center.y),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun VaultTriggerIcon(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val stroke = 1.8.dp.toPx()
        val w = size.width
        val h = size.height

        // Lock shackle
        drawArc(
            color = tint,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.25f, h * 0.1f),
            size = androidx.compose.ui.geometry.Size(w * 0.5f, h * 0.45f),
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
        // Lock body
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.15f, h * 0.4f),
            size = androidx.compose.ui.geometry.Size(w * 0.7f, h * 0.52f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()),
            style = Stroke(width = stroke)
        )
        // Keyhole
        drawCircle(
            color = tint,
            radius = 2.dp.toPx(),
            center = Offset(w / 2, h * 0.62f)
        )
    }
}
