package com.Vlynk.cifra.ui.calculator.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.Vlynk.cifra.ui.calculator.CalculatorAction

import androidx.compose.ui.res.stringResource
import com.Vlynk.cifra.R

/**
 * 4x5 Calculator keypad with equally sized buttons.
 */
@Composable
fun Keypad(
    onAction: (CalculatorAction) -> Unit,
    modifier: Modifier = Modifier,
    canBackspace: Boolean = false
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Row 1: ⌫/C, (), %, ÷
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            KeypadButton(
                text = if (canBackspace) "⌫" else "C",
                variant = KeypadButtonVariant.ACTION,
                contentDescription = stringResource(
                    if (canBackspace) R.string.desc_backspace else R.string.desc_clear
                ),
                onClick = {
                    if (canBackspace) {
                        onAction(CalculatorAction.Backspace)
                    } else {
                        onAction(CalculatorAction.Clear)
                    }
                },
                onLongClick = { onAction(CalculatorAction.Clear) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            KeypadButton(
                text = "()",
                variant = KeypadButtonVariant.ACTION,
                contentDescription = "Parentheses",
                onClick = { onAction(CalculatorAction.Parentheses) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            KeypadButton(
                text = "%",
                variant = KeypadButtonVariant.ACTION,
                contentDescription = "Percent",
                onClick = { onAction(CalculatorAction.Percent) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            KeypadButton(
                text = "÷",
                variant = KeypadButtonVariant.OPERATOR,
                contentDescription = "Divide",
                onClick = { onAction(CalculatorAction.Operator("÷")) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        // Row 2: 7, 8, 9, ×
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            KeypadButton(
                text = "7",
                variant = KeypadButtonVariant.NUMBER,
                onClick = { onAction(CalculatorAction.Digit(7)) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            KeypadButton(
                text = "8",
                variant = KeypadButtonVariant.NUMBER,
                onClick = { onAction(CalculatorAction.Digit(8)) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            KeypadButton(
                text = "9",
                variant = KeypadButtonVariant.NUMBER,
                onClick = { onAction(CalculatorAction.Digit(9)) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            KeypadButton(
                text = "×",
                variant = KeypadButtonVariant.OPERATOR,
                onClick = { onAction(CalculatorAction.Operator("×")) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        // Row 3: 4, 5, 6, −
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            KeypadButton(
                text = "4",
                variant = KeypadButtonVariant.NUMBER,
                onClick = { onAction(CalculatorAction.Digit(4)) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            KeypadButton(
                text = "5",
                variant = KeypadButtonVariant.NUMBER,
                onClick = { onAction(CalculatorAction.Digit(5)) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            KeypadButton(
                text = "6",
                variant = KeypadButtonVariant.NUMBER,
                onClick = { onAction(CalculatorAction.Digit(6)) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            KeypadButton(
                text = "−",
                variant = KeypadButtonVariant.OPERATOR,
                onClick = { onAction(CalculatorAction.Operator("−")) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        // Row 4: 1, 2, 3, +
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            KeypadButton(
                text = "1",
                variant = KeypadButtonVariant.NUMBER,
                onClick = { onAction(CalculatorAction.Digit(1)) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            KeypadButton(
                text = "2",
                variant = KeypadButtonVariant.NUMBER,
                onClick = { onAction(CalculatorAction.Digit(2)) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            KeypadButton(
                text = "3",
                variant = KeypadButtonVariant.NUMBER,
                onClick = { onAction(CalculatorAction.Digit(3)) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            KeypadButton(
                text = "+",
                variant = KeypadButtonVariant.OPERATOR,
                onClick = { onAction(CalculatorAction.Operator("+")) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        // Row 5: ±, 0, ., =
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            KeypadButton(
                text = "±",
                variant = KeypadButtonVariant.NUMBER,
                onClick = { onAction(CalculatorAction.ToggleSign) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            KeypadButton(
                text = "0",
                variant = KeypadButtonVariant.NUMBER,
                onClick = { onAction(CalculatorAction.Digit(0)) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            KeypadButton(
                text = ".",
                variant = KeypadButtonVariant.NUMBER,
                onClick = { onAction(CalculatorAction.Decimal) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            KeypadButton(
                text = "=",
                variant = KeypadButtonVariant.ACCENT,
                contentDescription = stringResource(R.string.desc_equals),
                onClick = { onAction(CalculatorAction.Equals) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
    }
}
