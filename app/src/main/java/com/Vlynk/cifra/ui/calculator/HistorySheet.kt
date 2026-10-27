package com.Vlynk.cifra.ui.calculator

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Vlynk.cifra.R
import com.Vlynk.cifra.domain.model.HistoryEntry
import com.Vlynk.cifra.ui.theme.ColorCarbon
import com.Vlynk.cifra.ui.theme.ColorCoralRed
import com.Vlynk.cifra.ui.theme.ColorFog
import com.Vlynk.cifra.ui.theme.ColorMist
import com.Vlynk.cifra.ui.theme.ColorPaper
import com.Vlynk.cifra.ui.theme.ColorSmoke

/**
 * Modal bottom sheet presenting previous calculations with tap-to-reuse and clear-all capabilities.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorySheet(
    history: List<HistoryEntry>,
    onReuseResult: (String) -> Unit,
    onClearAll: () -> Unit,
    onDismissRequest: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = ColorCarbon,
        dragHandle = {
            Surface(
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
                shape = RoundedCornerShape(2.dp),
                color = ColorSmoke
            ) {
                Box(modifier = Modifier.height(4.dp).fillMaxWidth(0.12f))
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .heightIn(min = 280.dp, max = 560.dp)
        ) {
            // Header Row: "History" and "Clear all"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.history_title),
                    color = ColorPaper,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                if (history.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ColorSmoke,
                        modifier = Modifier.clickable { onClearAll() }
                    ) {
                        Text(
                            text = stringResource(R.string.history_clear_all),
                            color = ColorPaper,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // History entries or empty state
            if (history.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.history_empty),
                        color = ColorFog,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(history) { entry ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onReuseResult(entry.result)
                                    onDismissRequest()
                                }
                                .padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = entry.expression,
                                color = ColorMist,
                                fontSize = 16.sp,
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.End
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val isError = entry.result.startsWith("Can't") || entry.result.startsWith("Error")
                            Text(
                                text = entry.result,
                                color = if (isError) ColorCoralRed else ColorPaper,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = ColorSmoke.copy(alpha = 0.5f))
                        }
                    }
                }
            }

            // Footer hint
            Text(
                text = stringResource(R.string.history_tap_to_reuse),
                color = ColorFog,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF16181F)
@Composable
private fun HistorySheetPreview() {
    com.Vlynk.cifra.ui.theme.CifraTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = stringResource(R.string.history_title),
                color = ColorPaper,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "1 250 + 750",
                    color = ColorMist,
                    fontSize = 16.sp
                )
                Text(
                    text = "2 000",
                    color = ColorPaper,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
