package com.notesync.ui.notes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    noteId: String?,
    // null o "new" = creazione; ID stringa = modifica
    onBack: () -> Unit,
    viewModel: NotesViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    // Se stiamo modificando, carica i dati della nota esistente
    val existingNote = remember(noteId, uiState.notes) {
        if (noteId != null && noteId != "new")
            uiState.notes.find { it.id == noteId }
        else null
    }
    var title by remember(existingNote) { mutableStateOf(existingNote?.title ?: "") }
    var content by remember(existingNote) { mutableStateOf(existingNote?.content ?: "") }
    val isNew = existingNote == null
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNew) "Nuova nota" else "Modifica nota") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Indietro"
                        )
                    }
                },
                actions = {
                    // Pulsante Salva: attivo solo se il titolo non e' vuoto
                    IconButton(
                        onClick = {
                            if (isNew) viewModel.createNote(title, content)
                            else
                                viewModel.updateNote(
                                    existingNote.id,
                                    title, content
                                )
                            onBack()
                        },
                        enabled = title.isNotBlank()
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Salva")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Titolo") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Contenuto") },
                modifier = Modifier.fillMaxSize(),
                maxLines = Int.MAX_VALUE
            )
        }
    }
}