package com.Vlynk.cifra.ui.tools

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.Vlynk.cifra.R
import com.Vlynk.cifra.ui.theme.ColorAcidLime
import com.Vlynk.cifra.ui.theme.ColorBone
import com.Vlynk.cifra.ui.theme.ColorCarbon
import com.Vlynk.cifra.ui.theme.ColorCoralRed
import com.Vlynk.cifra.ui.theme.ColorFog
import com.Vlynk.cifra.ui.theme.ColorSmoke
import com.Vlynk.cifra.ui.theme.ColorVoid

/**
 * Discount calculator screen.
 * Inputs: original price, discount percentage.
 * Outputs: final price and how much the user saves.
 */
@Composable
fun DiscountScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DiscountViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val errorInvalid = stringResource(R.string.discount_error_invalid)
    val errorRange   = stringResource(R.string.discount_error_pct_range)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ColorVoid)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // ── Top bar ──────────────────────────────────────────────────────────
        ToolTopBar(
            title = stringResource(R.string.discount_title),
            onBack = onNavigateBack
        )

        Spacer(modifier = Modifier.height(28.dp))

        // ── Inputs ───────────────────────────────────────────────────────────
        ToolInputField(
            label = stringResource(R.string.discount_label_price),
            value = state.price,
            hint = stringResource(R.string.discount_hint_price),
            keyboardType = KeyboardType.Decimal,
            onValueChange = viewModel::onPriceChange
        )

        Spacer(modifier = Modifier.height(14.dp))

        ToolInputField(
            label = stringResource(R.string.discount_label_pct),
            value = state.discountPct,
            hint = stringResource(R.string.discount_hint_pct),
            keyboardType = KeyboardType.Decimal,
            onValueChange = viewModel::onDiscountPctChange,
            trailingSuffix = "%"
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Error message
        if (state.error != null) {
            Text(
                text = state.error!!,
                color = ColorCoralRed,
                fontSize = 13.sp,
                modifier = Modifier.padding(vertical = 6.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(24.dp))
        }

        // ── Calculate button ─────────────────────────────────────────────────
        Button(
            onClick = { viewModel.calculate(errorInvalid, errorRange) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ColorAcidLime, contentColor = ColorVoid)
        ) {
            Text(text = stringResource(R.string.tool_calculate), fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        // ── Results card ─────────────────────────────────────────────────────
        if (state.finalPrice != null) {
            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = ColorCarbon,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Highlight: final price
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = stringResource(R.string.discount_result_final), color = ColorFog, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = state.finalPrice!!,
                            color = ColorAcidLime,
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    HorizontalDivider(color = ColorSmoke.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(16.dp))

                    // Saving row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.discount_result_saving),
                            color = ColorFog,
                            fontSize = 14.sp
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = ColorAcidLime.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "− ${state.saving}",
                                color = ColorAcidLime,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF08090A)
@Composable
private fun DiscountScreenPreview() {
    com.Vlynk.cifra.ui.theme.CifraTheme {
        DiscountScreen(onNavigateBack = {})
    }
}
