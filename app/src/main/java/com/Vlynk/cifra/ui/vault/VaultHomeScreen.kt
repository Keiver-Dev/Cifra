package com.Vlynk.cifra.ui.vault

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.Vlynk.cifra.R
import com.Vlynk.cifra.domain.model.Note
import com.Vlynk.cifra.ui.theme.ColorAcidLime
import com.Vlynk.cifra.ui.theme.ColorAsh
import com.Vlynk.cifra.ui.theme.ColorBone
import com.Vlynk.cifra.ui.theme.ColorCarbon
import com.Vlynk.cifra.ui.theme.ColorCoralRed
import com.Vlynk.cifra.ui.theme.ColorFog
import com.Vlynk.cifra.ui.theme.ColorGraphite
import com.Vlynk.cifra.ui.theme.ColorMist
import com.Vlynk.cifra.ui.theme.ColorObsidian
import com.Vlynk.cifra.ui.theme.ColorPaper
import com.Vlynk.cifra.ui.theme.ColorSmoke
import com.Vlynk.cifra.ui.theme.ColorVoid
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Vault home screen displaying secret notes, search, deletion with undo, and navigation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultHomeScreen(
    modifier: Modifier = Modifier,
    viewModel: VaultViewModel = viewModel(factory = VaultViewModel.Factory),
    onNoteClick: (Long) -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToTools: () -> Unit,
    onClose: () -> Unit
) {
    VaultPrivacyEffect()

    // Hardware/gesture back press returns directly to the Calculator
    androidx.activity.compose.BackHandler {
        onClose()
    }

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val noteDeletedMessage = stringResource(R.string.vault_note_deleted)
    val undoMessage = stringResource(R.string.vault_undo)

    LaunchedEffect(state.recentlyDeletedNote) {
        val deleted = state.recentlyDeletedNote
        if (deleted != null) {
            val result = snackbarHostState.showSnackbar(
                message = noteDeletedMessage,
                actionLabel = undoMessage,
                duration = SnackbarDuration.Short
            )
            when (result) {
                SnackbarResult.ActionPerformed -> viewModel.onUndoDelete()
                SnackbarResult.Dismissed -> viewModel.onDismissSnackbar()
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(ColorVoid)
            .statusBarsPadding(),
        containerColor = ColorVoid,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            VaultBottomNavigation(
                currentTab = VaultTab.NOTES,
                onTabSelect = { tab ->
                    if (tab == VaultTab.TOOLS) onNavigateToTools()
                }
            )
        },
        floatingActionButton = {
            Button(
                onClick = onNavigateToCreate,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorAcidLime,
                    contentColor = ColorVoid
                ),
                modifier = Modifier.height(48.dp)
            ) {
                Text(
                    text = stringResource(R.string.vault_new_note),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            // Header: Title + Settings + Close
            VaultHeader(
                onSettingsClick = onNavigateToSettings,
                onCloseClick = onClose
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            VaultSearchBar(
                query = state.searchQuery,
                onQueryChange = viewModel::onSearchQueryChange
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Notes List or Empty State
            if (state.notes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (state.searchQuery.isBlank()) {
                                stringResource(R.string.vault_empty_title)
                            } else {
                                stringResource(R.string.vault_no_results, state.searchQuery)
                            },
                            color = ColorPaper,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        if (state.searchQuery.isBlank()) {
                            Text(
                                text = stringResource(R.string.vault_empty_subtitle),
                                color = ColorFog,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(
                        items = state.notes,
                        key = { it.id }
                    ) { note ->
                        SwipeableNoteItem(
                            note = note,
                            onClick = { onNoteClick(note.id) },
                            onDelete = { viewModel.onDelete(note) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VaultHeader(
    onSettingsClick: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.vault_title),
            color = ColorPaper,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            // Settings button
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = ColorObsidian,
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onSettingsClick() }
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    GearIcon(tint = ColorBone, modifier = Modifier.size(18.dp))
                }
            }

            // Close button
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = ColorObsidian,
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onCloseClick() }
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CloseIcon(tint = ColorBone, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun VaultSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(ColorCarbon, RoundedCornerShape(8.dp))
            .border(1.dp, ColorGraphite, RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SearchIcon(tint = ColorFog, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(10.dp))

            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = stringResource(R.string.vault_search_placeholder),
                        color = ColorFog,
                        fontSize = 15.sp
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        color = ColorPaper,
                        fontSize = 15.sp
                    ),
                    cursorBrush = SolidColor(ColorAcidLime),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (query.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onQueryChange("") },
                    contentAlignment = Alignment.Center
                ) {
                    CloseIcon(tint = ColorFog, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@Composable
private fun NoteItemRow(
    note: Note,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp)
    ) {
        // Title
        Text(
            text = note.title.ifBlank { "Untitled" },
            color = ColorPaper,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Content preview
        if (note.content.isNotBlank()) {
            Text(
                text = note.content,
                color = ColorFog,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Date + Category Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatNoteDate(note.updatedAt),
                color = ColorAsh,
                fontSize = 13.sp
            )

            if (!note.category.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = ColorSmoke
                ) {
                    Text(
                        text = note.category,
                        color = ColorMist,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = ColorSmoke.copy(alpha = 0.4f))
    }
}

/**
 * Swipeable wrapper around [NoteItemRow] that reveals a red delete background
 * on a right-to-left (EndToStart) swipe and triggers [onDelete] on completion.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableNoteItem(
    note: Note,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        },
        positionalThreshold = { totalDistance -> totalDistance * 0.4f }
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val fraction = dismissState.progress
            val bgColor by animateColorAsState(
                targetValue = if (fraction > 0.05f) ColorCoralRed.copy(alpha = (fraction * 2f).coerceAtMost(1f))
                else Color.Transparent,
                label = "swipe_bg"
            )
            val scale by animateFloatAsState(
                targetValue = if (fraction > 0.1f) 0.85f + (fraction * 0.15f).coerceAtMost(0.15f) else 0.75f,
                label = "swipe_icon_scale"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bgColor, RoundedCornerShape(8.dp))
                    .padding(end = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Box(modifier = Modifier.scale(scale)) {
                    TrashIconSmall(tint = ColorPaper, modifier = Modifier.size(22.dp))
                }
            }
        }
    ) {
        NoteItemRow(note = note, onClick = onClick)
    }
}

@Composable
private fun TrashIconSmall(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val stroke = 2.dp.toPx()
        val w = size.width; val h = size.height
        drawLine(tint, Offset(w * 0.15f, h * 0.25f), Offset(w * 0.85f, h * 0.25f), stroke, StrokeCap.Round)
        drawLine(tint, Offset(w * 0.35f, h * 0.15f), Offset(w * 0.65f, h * 0.15f), stroke, StrokeCap.Round)
        drawLine(tint, Offset(w * 0.25f, h * 0.25f), Offset(w * 0.3f, h * 0.9f), stroke, StrokeCap.Round)
        drawLine(tint, Offset(w * 0.3f, h * 0.9f), Offset(w * 0.7f, h * 0.9f), stroke, StrokeCap.Round)
        drawLine(tint, Offset(w * 0.75f, h * 0.25f), Offset(w * 0.7f, h * 0.9f), stroke, StrokeCap.Round)
    }
}

enum class VaultTab {
    NOTES,
    TOOLS
}

@Composable
private fun VaultBottomNavigation(
    currentTab: VaultTab,
    onTabSelect: (VaultTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = ColorVoid
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Notes Tab
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTabSelect(VaultTab.NOTES) }
                    .padding(vertical = 6.dp)
            ) {
                Text(
                    text = stringResource(R.string.vault_tab_notes),
                    color = if (currentTab == VaultTab.NOTES) ColorPaper else ColorFog,
                    fontSize = 14.sp,
                    fontWeight = if (currentTab == VaultTab.NOTES) FontWeight.Bold else FontWeight.Normal
                )
                if (currentTab == VaultTab.NOTES) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(2.dp)
                            .background(ColorAcidLime, RoundedCornerShape(1.dp))
                    )
                }
            }

            // Tools Tab
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTabSelect(VaultTab.TOOLS) }
                    .padding(vertical = 6.dp)
            ) {
                Text(
                    text = stringResource(R.string.vault_tab_tools),
                    color = if (currentTab == VaultTab.TOOLS) ColorPaper else ColorFog,
                    fontSize = 14.sp,
                    fontWeight = if (currentTab == VaultTab.TOOLS) FontWeight.Bold else FontWeight.Normal
                )
                if (currentTab == VaultTab.TOOLS) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(2.dp)
                            .background(ColorAcidLime, RoundedCornerShape(1.dp))
                    )
                }
            }
        }
    }
}

private fun formatNoteDate(timestamp: Long): String {
    val date = Date(timestamp)
    val formatter = SimpleDateFormat("MMM d", Locale.getDefault())
    return formatter.format(date)
}

// --- Custom Vector Icons ---

@Composable
private fun GearIcon(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val stroke = 1.6.dp.toPx()
        val radius = size.minDimension / 2 - stroke
        val center = Offset(size.width / 2, size.height / 2)
        drawCircle(
            color = tint,
            radius = radius * 0.45f,
            center = center,
            style = Stroke(width = stroke)
        )
        drawCircle(
            color = tint,
            radius = radius,
            center = center,
            style = Stroke(width = stroke)
        )
    }
}

@Composable
private fun CloseIcon(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val stroke = 1.8.dp.toPx()
        drawLine(
            color = tint,
            start = Offset(0f, 0f),
            end = Offset(size.width, size.height),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = Offset(size.width, 0f),
            end = Offset(0f, size.height),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun SearchIcon(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val stroke = 1.6.dp.toPx()
        val circleRadius = size.minDimension * 0.35f
        val circleCenter = Offset(circleRadius + stroke, circleRadius + stroke)

        drawCircle(
            color = tint,
            radius = circleRadius,
            center = circleCenter,
            style = Stroke(width = stroke)
        )
        drawLine(
            color = tint,
            start = Offset(circleCenter.x + circleRadius * 0.7f, circleCenter.y + circleRadius * 0.7f),
            end = Offset(size.width - stroke, size.height - stroke),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF08090A)
@Composable
private fun VaultHomeScreenPreview() {
    com.Vlynk.cifra.ui.theme.CifraTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ColorVoid)
                .padding(20.dp)
        ) {
            VaultHeader(onSettingsClick = {}, onCloseClick = {})
            Spacer(Modifier.height(16.dp))
            VaultSearchBar(query = "", onQueryChange = {})
            Spacer(Modifier.height(16.dp))
            NoteItemRow(
                note = com.Vlynk.cifra.domain.model.Note(
                    id = 1L,
                    title = "Secret Passwords",
                    content = "Server root: admin/secret123",
                    createdAt = System.currentTimeMillis()
                ),
                onClick = {}
            )
        }
    }
}
