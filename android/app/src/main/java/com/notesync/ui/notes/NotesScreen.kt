package com.notesync.ui.notes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.notesync.data.local.SyncStatus
import com.notesync.domain.model.Note
import org.koin.androidx.compose.koinViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    onNavigateToDetail: (String?) -> Unit,
    onLogout: () -> Unit,
    viewModel: NotesViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(uiState.shouldLogout) {
        if (uiState.shouldLogout) {
            viewModel.onLogoutHandled()
            onLogout()
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NoteSync") },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Sincronizza")
                    }
                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToDetail(null) }) {
                Icon(Icons.Default.Add, contentDescription = "Nuova nota")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                uiState.isLoading ->
                    CircularProgressIndicator(modifier =
                        Modifier.align(Alignment.Center))
                uiState.notes.isEmpty() ->
                    Text("Nessuna nota. Premi + per crearne una!",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge)
                else ->
                    LazyColumn {
                        items(uiState.notes, key = { it.id }) { note ->
                            NoteItem(note = note,
                                onClick = { onNavigateToDetail(note.id) },
                                onDelete = { viewModel.deleteNote(note.id) })
                        }
                    }
            }
        }
        // Mostra errori come SnackBar (semplificato)
        uiState.error?.let {
            LaunchedEffect(it) { viewModel.clearError() }
        }
    }
}
// ── NoteItem: card per ogni nota ─────────────────────────────────────────
@Composable
fun NoteItem(note: Note, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical =
            4.dp),
        onClick = onClick
    ) {
        Row(modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(note.title, style = MaterialTheme.typography.titleMedium)
                if (note.content.isNotBlank()) {
                    val preview = note.content.take(80) +
                            if (note.content.length > 80) "..." else ""
                    Text(preview, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            SyncStatusIcon(note.syncStatus)
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Elimina",
                    tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
// ── SyncStatusIcon ────────────────────────────────────────────────────────
@Composable
fun SyncStatusIcon(syncStatus: SyncStatus) {
    when (syncStatus) {
        SyncStatus.SYNCED ->
            Icon(Icons.Default.CloudDone, contentDescription = "Sincronizzata",
                tint = Color(0xFF4CAF50))
        SyncStatus.PENDING_CREATE, SyncStatus.PENDING_UPDATE ->
            Icon(Icons.Default.CloudOff, contentDescription = "Solo in locale",
                tint = Color(0xFFFFC107))
        else ->
            Icon(Icons.Default.Sync, contentDescription = "In sincronizzazione",
                tint = MaterialTheme.colorScheme.primary)
    }
}