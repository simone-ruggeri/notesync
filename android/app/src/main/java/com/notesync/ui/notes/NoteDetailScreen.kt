package com.notesync.ui.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.notesync.data.local.SyncStatus
import com.notesync.ui.theme.LocalBg
import com.notesync.ui.theme.LocalText
import com.notesync.ui.theme.SlatePrimary
import com.notesync.ui.theme.SyncedBg
import com.notesync.ui.theme.SyncedText
import com.notesync.ui.theme.TextPrimary
import com.notesync.ui.theme.TextSecondary
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NoteDetailScreen(
    mode: NoteMode,
    onBack: () -> Unit,
    viewModel: NoteDetailViewModel = koinViewModel(parameters = { parametersOf(mode) })
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.savedSuccessfully) {
        if (uiState.savedSuccessfully) onBack()
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    val isSynced = uiState.syncStatus == SyncStatus.SYNCED

    // containerColor = SlatePrimary fa sì che l'area della status bar (sopra il contenuto)
    // abbia lo stesso colore scuro del topbar, senza fare edge-to-edge esplicito.
    Scaffold(
        containerColor = SlatePrimary,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ── Topbar scura ────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SlatePrimary)
                    .padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Indietro",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = if (mode is NoteMode.Create) "Nuova nota" else "Modifica nota",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = { viewModel.save() },
                    enabled = uiState.title.isNotBlank() && !uiState.isSaving,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Salva",
                        tint = if (uiState.title.isNotBlank()) Color.White
                               else Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // ── Barra stato sync ─────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isSynced) SyncedBg else LocalBg)
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isSynced) Icons.Default.CloudDone else Icons.Default.CloudOff,
                    contentDescription = null,
                    tint = if (isSynced) SyncedText else LocalText,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isSynced) "Sincronizzata · salvata automaticamente"
                           else "Salvata in locale · non sincronizzata",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSynced) SyncedText else LocalText
                )
            }

            // ── Editor body ─────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                // Titolo
                BasicTextField(
                    value = uiState.title,
                    onValueChange = { viewModel.onTitleChange(it) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.titleLarge.copy(color = TextPrimary),
                    cursorBrush = SolidColor(SlatePrimary),
                    decorationBox = { innerTextField ->
                        Box {
                            if (uiState.title.isEmpty()) {
                                Text(
                                    text = "Titolo...",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color(0xFFCCCCCC)
                                )
                            }
                            innerTextField()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                )

                // Data di modifica
                Text(
                    text = formatDetailDate(uiState.updatedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFBBBBBB),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                HorizontalDivider(
                    color = Color(0xFFF0EEE9),
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(bottom = 14.dp)
                )

                // Contenuto
                BasicTextField(
                    value = uiState.content,
                    onValueChange = { viewModel.onContentChange(it) },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF3A3A4A),
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                    ),
                    cursorBrush = SolidColor(SlatePrimary),
                    decorationBox = { innerTextField ->
                        Box {
                            if (uiState.content.isEmpty()) {
                                Text(
                                    text = "Inizia a scrivere...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFFCCCCCC)
                                )
                            }
                            innerTextField()
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

private fun formatDetailDate(timestamp: Long): String {
    val format = SimpleDateFormat("d MMM, HH:mm", Locale.ITALIAN)
    return format.format(Date(timestamp))
}
