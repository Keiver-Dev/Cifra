package com.Vlynk.cifra.ui.calculator.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Vlynk.cifra.ui.theme.ColorCoralRed
import com.Vlynk.cifra.ui.theme.ColorMist
import com.Vlynk.cifra.ui.theme.ColorPaper

/**
 * Calculator display showing expression on top and large adaptive result/error below.
 */
@Composable
fun Display(
    expression: String,
    result: String,
    error: String?,
    errorRes: Int?,
    modifier: Modifier = Modifier
) {
    val expressionScrollState = rememberScrollState()

    // Automatically scroll to the end of the expression as the user types
    LaunchedEffect(expression) {
        if (expression.isNotEmpty()) {
            expressionScrollState.animateScrollTo(expressionScrollState.maxValue)
        }
    }

    val displayError = errorRes?.let { stringResource(it) } ?: error

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Bottom
    ) {
        // Upper row: Current expression in monospace
        Text(
            text = expression.ifEmpty { " " },
            color = ColorMist,
            fontSize = 22.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.End,
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(expressionScrollState)
        )

        // Lower row: Large result or prominent error
        if (displayError != null) {
            Text(
                text = displayError,
                color = ColorCoralRed,
                fontSize = if (displayError.length > 18) 28.sp else 34.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        } else {
            val resultFontSize = when {
                result.length > 16 -> 32.sp
                result.length > 12 -> 40.sp
                result.length > 8 -> 52.sp
                else -> 64.sp
            }

            Text(
                text = result.ifEmpty { " " },
                color = ColorPaper,
                fontSize = resultFontSize,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }
    }
}
