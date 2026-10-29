package com.Vlynk.cifra.ui.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
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
import com.Vlynk.cifra.ui.theme.ColorGraphite
import com.Vlynk.cifra.ui.theme.ColorObsidian
import com.Vlynk.cifra.ui.theme.ColorPaper
import com.Vlynk.cifra.ui.theme.ColorSmoke
import com.Vlynk.cifra.ui.theme.ColorVoid

/**
 * Tip & Split calculator screen.
 * Inputs: bill total, tip %, number of people.
 * Outputs: per-person amount, total tip, grand total.
 * Uses [TipSplitViewModel] backed by [BigDecimal] for exact monetary math.
 */
@Composable
fun TipSplitScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TipSplitViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val errorInvalid = stringResource(R.string.tip_error_invalid)
    val errorPeople  = stringResource(R.string.tip_error_people_zero)

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
            title = stringResource(R.string.tip_title),
            onBack = onNavigateBack
        )

        Spacer(modifier = Modifier.height(28.dp))

        // ── Inputs ───────────────────────────────────────────────────────────
        ToolInputField(
            label = stringResource(R.string.tip_label_total),
            value = state.total,
            hint = stringResource(R.string.tip_hint_amount),
            keyboardType = KeyboardType.Decimal,
            onValueChange = viewModel::onTotalChange
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                ToolInputField(
                    label = stringResource(R.string.tip_label_tip_pct),
                    value = state.tipPercent,
                    hint = stringResource(R.string.tip_hint_percent),
                    keyboardType = KeyboardType.Decimal,
                    onValueChange = viewModel::onTipPercentChange,
                    trailingSuffix = "%"
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                ToolInputField(
                    label = stringResource(R.string.tip_label_people),
                    value = state.people,
                    hint = stringResource(R.string.tip_hint_people),
                    keyboardType = KeyboardType.Number,
                    onValueChange = viewModel::onPeopleChange,
                    trailingSuffix = "👤"
                )
            }
        }

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
            onClick = { viewModel.calculate(errorInvalid, errorPeople) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ColorAcidLime, contentColor = ColorVoid)
        ) {
            Text(
                text = stringResource(R.string.tool_calculate),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // ── Results card ─────────────────────────────────────────────────────
        if (state.perPerson != null) {
            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = ColorCarbon,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Highlight: per person
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(text = stringResource(R.string.tip_result_per_person), color = ColorFog, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = state.perPerson!!,
                            color = ColorAcidLime,
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    HorizontalDivider(color = ColorSmoke.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(16.dp))

                    ResultRow(label = stringResource(R.string.tip_result_tip_total), value = state.tipTotal ?: "")
                    Spacer(modifier = Modifier.height(8.dp))
                    ResultRow(label = stringResource(R.string.tip_result_grand_total), value = state.grandTotal ?: "")
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
internal fun ToolTopBar(title: String, onBack: () -> Unit) {
    val backDesc = stringResource(R.string.desc_back)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = ColorObsidian,
            modifier = Modifier
                .size(40.dp)
                .clickable(onClickLabel = backDesc) { onBack() }
                .semantics { contentDescription = backDesc }
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                BackArrowIcon(tint = ColorBone, modifier = Modifier.size(16.dp))
            }
        }
        Text(text = title, color = ColorPaper, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.size(40.dp))
    }
}

@Composable
internal fun ToolInputField(
    label: String,
    value: String,
    hint: String,
    keyboardType: KeyboardType,
    onValueChange: (String) -> Unit,
    trailingSuffix: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(text = label, color = ColorFog, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(6.dp))
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = ColorCarbon,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(text = hint, color = ColorGraphite, fontSize = 16.sp)
                    }
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        singleLine = true,
                        textStyle = TextStyle(color = ColorPaper, fontSize = 16.sp, fontWeight = FontWeight.Medium),
                        cursorBrush = SolidColor(ColorAcidLime),
                        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (trailingSuffix != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = trailingSuffix, color = ColorFog, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
private fun ResultRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = ColorFog, fontSize = 14.sp)
        Text(text = value, color = ColorBone, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}


@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF08090A)
@Composable
private fun TipSplitScreenPreview() {
    com.Vlynk.cifra.ui.theme.CifraTheme {
        TipSplitScreen(onNavigateBack = {})
    }
}
