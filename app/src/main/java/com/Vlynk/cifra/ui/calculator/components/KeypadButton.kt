package com.Vlynk.cifra.ui.calculator.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Vlynk.cifra.ui.theme.ColorAcidLime
import com.Vlynk.cifra.ui.theme.ColorBone
import com.Vlynk.cifra.ui.theme.ColorMist
import com.Vlynk.cifra.ui.theme.ColorObsidian
import com.Vlynk.cifra.ui.theme.ColorPaper
import com.Vlynk.cifra.ui.theme.ColorVoid

/**
 * Visual variant for buttons on the calculator keypad.
 */
enum class KeypadButtonVariant {
    NUMBER,
    OPERATOR,
    ACTION,
    ACCENT
}

/**
 * Reusable keypad button with comfortable touch target and distinct visual variants.
 */
@Composable
fun KeypadButton(
    text: String,
    variant: KeypadButtonVariant,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = when (variant) {
        KeypadButtonVariant.NUMBER -> ColorObsidian to ColorPaper
        KeypadButtonVariant.OPERATOR -> ColorObsidian to ColorBone
        KeypadButtonVariant.ACTION -> ColorObsidian to ColorMist
        KeypadButtonVariant.ACCENT -> ColorAcidLime to ColorVoid
    }

    Surface(
        modifier = modifier
            .defaultMinSize(minWidth = 56.dp, minHeight = 56.dp)
            .padding(4.dp),
        shape = RoundedCornerShape(14.dp),
        color = backgroundColor,
        onClick = onClick
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = textColor,
                fontSize = if (variant == KeypadButtonVariant.ACTION && text.length > 1) 22.sp else 26.sp,
                fontWeight = if (variant == KeypadButtonVariant.ACCENT) FontWeight.Bold else FontWeight.Medium,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
