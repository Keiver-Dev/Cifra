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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.Vlynk.cifra.ui.theme.ColorFog
import com.Vlynk.cifra.ui.theme.ColorObsidian
import com.Vlynk.cifra.ui.theme.ColorPaper
import com.Vlynk.cifra.ui.theme.ColorVoid

@Composable
fun UnitConverterScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: UnitConverterViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

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
        // Top bar
        ToolTopBar(
            title = stringResource(R.string.unit_title),
            onBack = onNavigateBack
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Category Pills (Length, Weight, Temperature)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CategoryPill(
                title = stringResource(R.string.unit_tab_length),
                selected = state.category == UnitCategory.LENGTH,
                onClick = { viewModel.onCategorySelect(UnitCategory.LENGTH) },
                modifier = Modifier.weight(1f)
            )
            CategoryPill(
                title = stringResource(R.string.unit_tab_weight),
                selected = state.category == UnitCategory.WEIGHT,
                onClick = { viewModel.onCategorySelect(UnitCategory.WEIGHT) },
                modifier = Modifier.weight(1f)
            )
            CategoryPill(
                title = stringResource(R.string.unit_tab_temperature),
                selected = state.category == UnitCategory.TEMPERATURE,
                onClick = { viewModel.onCategorySelect(UnitCategory.TEMPERATURE) },
                modifier = Modifier.weight(1.1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // From Card
        val availableUnits = when (state.category) {
            UnitCategory.LENGTH -> UnitConverterViewModel.lengthUnits
            UnitCategory.WEIGHT -> UnitConverterViewModel.weightUnits
            UnitCategory.TEMPERATURE -> UnitConverterViewModel.tempUnits
        }

        UnitCard(
            label = stringResource(R.string.unit_label_from),
            value = state.inputValue,
            isEditable = true,
            onValueChange = viewModel::onInputValueChange,
            selectedUnit = state.fromUnit,
            units = availableUnits,
            onUnitSelect = viewModel::onFromUnitChange
        )

        // Swap button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            val swapDesc = stringResource(R.string.desc_swap)
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = ColorCarbon,
                modifier = Modifier
                    .size(42.dp)
                    .clickable(onClickLabel = swapDesc) { viewModel.swapUnits() }
                    .semantics { contentDescription = swapDesc }
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(text = "⇅", color = ColorAcidLime, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // To Card
        UnitCard(
            label = stringResource(R.string.unit_label_to),
            value = state.convertedValue,
            isEditable = false,
            onValueChange = {},
            selectedUnit = state.toUnit,
            units = availableUnits,
            onUnitSelect = viewModel::onToUnitChange
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Equivalence hint
        if (state.equivalenceText.isNotEmpty()) {
            Text(
                text = state.equivalenceText,
                color = ColorFog,
                fontSize = 13.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun CategoryPill(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (selected) ColorPaper else ColorCarbon,
        modifier = modifier
            .height(38.dp)
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = title,
                color = if (selected) ColorVoid else ColorFog,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun UnitCard(
    label: String,
    value: String,
    isEditable: Boolean,
    onValueChange: (String) -> Unit,
    selectedUnit: String,
    units: List<String>,
    onUnitSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = ColorCarbon,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(text = label, color = ColorFog, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (isEditable) {
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = TextStyle(
                            color = ColorPaper,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        cursorBrush = SolidColor(ColorAcidLime),
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Text(
                        text = value,
                        color = ColorPaper,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Unit Selector Pill
                Box {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ColorObsidian,
                        modifier = Modifier
                            .clickable { expanded = true }
                            .padding(vertical = 4.dp, horizontal = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = selectedUnit,
                                color = ColorBone,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "▾", color = ColorFog, fontSize = 13.sp)
                        }
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(ColorObsidian)
                    ) {
                        units.forEach { unit ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = unit,
                                        color = if (unit == selectedUnit) ColorAcidLime else ColorPaper,
                                        fontWeight = if (unit == selectedUnit) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    onUnitSelect(unit)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
