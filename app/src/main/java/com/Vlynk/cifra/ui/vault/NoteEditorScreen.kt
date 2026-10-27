package com.Vlynk.cifra.ui.vault

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.Vlynk.cifra.CifraApp
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Screen to edit an existing secret note or create a new one.
 */
@Composable
fun NoteEditorScreen(
    noteId: Long?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NoteEditorViewModel = viewModel(
        factory = NoteEditorViewModel.provideFactory(
            noteId = noteId,
            app = LocalContext.current.applicationContext as CifraApp
        )
    )
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Automatically navigate back once note is saved or deleted
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            onNavigateBack()
        }
    }

    // Intercept hardware/gesture back to auto-save unsaved changes
    BackHandler {
        viewModel.saveOnBack()
        onNavigateBack()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ColorVoid)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Top Bar: Back + Delete (if existing) + Save button
            EditorTopBar(
                isNew = state.isNew,
                onBackClick = {
                    viewModel.saveOnBack()
                    onNavigateBack()
                },
                onDeleteClick = { viewModel.showDeleteDialog(true) },
                onSaveClick = { viewModel.save() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Title Input
            BasicTextField(
                value = state.title,
                onValueChange = viewModel::onTitleChange,
                singleLine = false,
                maxLines = 2,
                textStyle = TextStyle(
                    color = ColorPaper,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 32.sp
                ),
                cursorBrush = SolidColor(ColorAcidLime),
                decorationBox = { innerTextField ->
                    if (state.title.isEmpty()) {
                        Text(
                            text = stringResource(R.string.editor_title_hint),
                            color = ColorFog,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    innerTextField()
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Body Content Input
            BasicTextField(
                value = state.content,
                onValueChange = viewModel::onContentChange,
                textStyle = TextStyle(
                    color = ColorBone,
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                ),
                cursorBrush = SolidColor(ColorAcidLime),
                decorationBox = { innerTextField ->
                    if (state.content.isEmpty()) {
                        Text(
                            text = stringResource(R.string.editor_content_hint),
                            color = ColorFog.copy(alpha = 0.7f),
                            fontSize = 16.sp,
                            lineHeight = 24.sp
                        )
                    }
                    innerTextField()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            HorizontalDivider(color = ColorSmoke.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(10.dp))

            // Footer info: Created/Edited date & Character count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatEditorDate(state.createdAt, state.updatedAt),
                    color = ColorFog,
                    fontSize = 12.sp
                )

                Text(
                    text = stringResource(R.string.editor_chars, state.charCount),
                    color = ColorFog,
                    fontSize = 12.sp
                )
            }
        }

        // Delete Confirmation Dialog
        if (state.showDeleteDialog) {
            DeleteNoteDialog(
                noteTitle = state.title,
                onConfirm = { viewModel.delete() },
                onDismiss = { viewModel.showDeleteDialog(false) }
            )
        }
    }
}

@Composable
private fun EditorTopBar(
    isNew: Boolean,
    onBackClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back Button
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = ColorObsidian,
            modifier = Modifier
                .size(40.dp)
                .clickable(onClick = onBackClick)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                BackArrowIcon(tint = ColorBone, modifier = Modifier.size(16.dp))
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Delete Button (only for existing notes)
            if (!isNew) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ColorObsidian,
                    modifier = Modifier
                        .size(40.dp)
                        .clickable(onClick = onDeleteClick)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        TrashIcon(tint = ColorCoralRed, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Save Button
            Button(
                onClick = onSaveClick,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorAcidLime,
                    contentColor = ColorVoid
                ),
                modifier = Modifier.height(40.dp)
            ) {
                Text(
                    text = stringResource(R.string.editor_save),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun DeleteNoteDialog(
    noteTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = ColorCarbon,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = stringResource(R.string.editor_delete_dialog_title),
                    color = ColorPaper,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = stringResource(
                        R.string.editor_delete_dialog_desc,
                        noteTitle.ifBlank { "This note" }
                    ),
                    color = ColorFog,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    // Cancel button
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ColorGraphite,
                        modifier = Modifier
                            .clickable(onClick = onDismiss)
                            .height(40.dp)
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.editor_delete_dialog_cancel),
                                color = ColorPaper,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Delete button
                    Button(
                        onClick = onConfirm,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ColorCoralRed,
                            contentColor = ColorPaper
                        ),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.editor_delete_dialog_confirm),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private fun formatEditorDate(createdAt: Long, updatedAt: Long): String {
    if (createdAt == 0L) return "New note"
    val date = Date(updatedAt.takeIf { it != 0L } ?: createdAt)
    val formatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    return "Edited ${formatter.format(date)}"
}

// --- Icons ---

@Composable
private fun BackArrowIcon(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val stroke = 2.dp.toPx()
        val midY = size.height / 2
        // horizontal line
        drawLine(
            color = tint,
            start = Offset(0f, midY),
            end = Offset(size.width, midY),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        // upper diagonal
        drawLine(
            color = tint,
            start = Offset(0f, midY),
            end = Offset(size.width * 0.45f, midY - size.height * 0.4f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        // lower diagonal
        drawLine(
            color = tint,
            start = Offset(0f, midY),
            end = Offset(size.width * 0.45f, midY + size.height * 0.4f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun TrashIcon(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val stroke = 1.8.dp.toPx()
        val w = size.width
        val h = size.height

        // Lid
        drawLine(
            color = tint,
            start = Offset(w * 0.15f, h * 0.25f),
            end = Offset(w * 0.85f, h * 0.25f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        // Handle
        drawLine(
            color = tint,
            start = Offset(w * 0.35f, h * 0.15f),
            end = Offset(w * 0.65f, h * 0.15f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        // Body
        drawLine(
            color = tint,
            start = Offset(w * 0.25f, h * 0.25f),
            end = Offset(w * 0.3f, h * 0.9f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = Offset(w * 0.3f, h * 0.9f),
            end = Offset(w * 0.7f, h * 0.9f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = Offset(w * 0.75f, h * 0.25f),
            end = Offset(w * 0.7f, h * 0.9f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}
