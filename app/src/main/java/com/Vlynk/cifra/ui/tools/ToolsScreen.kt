package com.Vlynk.cifra.ui.tools

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Vlynk.cifra.R
import com.Vlynk.cifra.ui.theme.ColorAcidLime
import com.Vlynk.cifra.ui.theme.ColorBone
import com.Vlynk.cifra.ui.theme.ColorCarbon
import com.Vlynk.cifra.ui.theme.ColorFog
import com.Vlynk.cifra.ui.theme.ColorObsidian
import com.Vlynk.cifra.ui.theme.ColorPaper
import com.Vlynk.cifra.ui.theme.ColorSmoke
import com.Vlynk.cifra.ui.theme.ColorVoid

/**
 * Tools menu screen showing available calculator utilities.
 * Each tool card navigates to a dedicated calculator screen.
 */
@Composable
fun ToolsScreen(
    onNavigateToTipSplit: () -> Unit,
    onNavigateToDiscount: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ColorVoid)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Top bar
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
                    .clickable { onNavigateBack() }
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    BackArrowIcon(tint = ColorBone, modifier = Modifier.size(16.dp))
                }
            }

            Text(
                text = stringResource(R.string.tools_title),
                color = ColorPaper,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.size(40.dp))
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.tools_subtitle),
            color = ColorFog,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Tool cards with stagger animation
        ToolCard(
            icon = { TipIcon(tint = ColorAcidLime, modifier = Modifier.size(26.dp)) },
            title = stringResource(R.string.tools_tip_split_title),
            description = stringResource(R.string.tools_tip_split_desc),
            onClick = onNavigateToTipSplit,
            delayMs = 0
        )

        Spacer(modifier = Modifier.height(12.dp))

        ToolCard(
            icon = { DiscountIconSmall(tint = ColorAcidLime, modifier = Modifier.size(26.dp)) },
            title = stringResource(R.string.tools_discount_title),
            description = stringResource(R.string.tools_discount_desc),
            onClick = onNavigateToDiscount,
            delayMs = 80
        )
    }
}

@Composable
private fun ToolCard(
    icon: @Composable () -> Unit,
    title: String,
    description: String,
    onClick: () -> Unit,
    delayMs: Int = 0,
    modifier: Modifier = Modifier
) {
    var targetAlpha by remember { mutableFloatStateOf(0f) }
    var targetScale by remember { mutableFloatStateOf(0.95f) }

    val alpha by animateFloatAsState(targetValue = targetAlpha, animationSpec = tween(300, delayMs), label = "alpha")
    val scale by animateFloatAsState(targetValue = targetScale, animationSpec = tween(300, delayMs), label = "scale")

    LaunchedEffect(Unit) { targetAlpha = 1f; targetScale = 1f }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .alpha(alpha)
            .scale(scale)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = ColorCarbon
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = ColorObsidian,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    icon()
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = ColorPaper, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(3.dp))
                Text(text = description, color = ColorFog, fontSize = 13.sp, lineHeight = 18.sp)
            }

            ChevronRightIcon(tint = ColorSmoke, modifier = Modifier.size(16.dp))
        }
    }
}

// ─── Icons ────────────────────────────────────────────────────────────────────

@Composable
internal fun BackArrowIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val stroke = 2.dp.toPx(); val midY = size.height / 2
        drawLine(tint, Offset(0f, midY), Offset(size.width, midY), stroke, StrokeCap.Round)
        drawLine(tint, Offset(0f, midY), Offset(size.width * 0.45f, midY - size.height * 0.4f), stroke, StrokeCap.Round)
        drawLine(tint, Offset(0f, midY), Offset(size.width * 0.45f, midY + size.height * 0.4f), stroke, StrokeCap.Round)
    }
}

@Composable
private fun ChevronRightIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val stroke = 2.dp.toPx(); val midY = size.height / 2
        drawLine(tint, Offset(size.width * 0.2f, midY - size.height * 0.35f), Offset(size.width * 0.75f, midY), stroke, StrokeCap.Round)
        drawLine(tint, Offset(size.width * 0.75f, midY), Offset(size.width * 0.2f, midY + size.height * 0.35f), stroke, StrokeCap.Round)
    }
}

@Composable
private fun TipIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val stroke = 2.dp.toPx(); val w = size.width; val h = size.height
        drawCircle(tint, radius = w * 0.32f, center = Offset(w / 2, h * 0.38f), style = Stroke(stroke))
        drawLine(tint, Offset(w * 0.38f, h * 0.62f), Offset(w * 0.62f, h * 0.62f), stroke, StrokeCap.Round)
        drawLine(tint, Offset(w * 0.3f, h * 0.74f), Offset(w * 0.7f, h * 0.74f), stroke, StrokeCap.Round)
        drawLine(tint, Offset(w / 2, h * 0.62f), Offset(w / 2, h * 0.88f), stroke, StrokeCap.Round)
    }
}

@Composable
private fun DiscountIconSmall(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val stroke = 2.dp.toPx(); val w = size.width; val h = size.height
        drawLine(tint, Offset(w * 0.1f, h * 0.9f), Offset(w * 0.9f, h * 0.1f), stroke, StrokeCap.Round)
        drawCircle(tint, radius = stroke * 1.6f, center = Offset(w * 0.27f, h * 0.28f))
        drawCircle(tint, radius = stroke * 1.6f, center = Offset(w * 0.73f, h * 0.75f))
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF08090A)
@Composable
private fun ToolsScreenPreview() {
    com.Vlynk.cifra.ui.theme.CifraTheme {
        ToolsScreen(onNavigateToTipSplit = {}, onNavigateToDiscount = {}, onNavigateBack = {})
    }
}
