package com.example.localagent.ui.setup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.localagent.llm.EngineLoadState
import com.example.localagent.model.ModelScanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    onModelLoaded: () -> Unit,
    viewModel: SetupViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.loadComplete) {
        if (uiState.loadComplete) onModelLoaded()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Memory, contentDescription = null, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Local Agent", fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.scanForModels() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Rescan")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Select a model to load", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            if (uiState.isScanning) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Scanning directories...", style = MaterialTheme.typography.bodyMedium)
                }
            }

            AnimatedVisibility(visible = uiState.engineState is EngineLoadState.Loading) {
                val loading = uiState.engineState as? EngineLoadState.Loading
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(loading?.message ?: "Loading model...", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            }

            AnimatedVisibility(visible = uiState.error != null && uiState.engineState !is EngineLoadState.Loading) {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(uiState.error ?: "", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                items(uiState.models, key = { it.ptePath.absolutePath }) { model ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = if (model == uiState.selectedModel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface),
                        onClick = { viewModel.selectModel(model) }
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(model.name, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                                Text("Model: ${model.displaySize} | Tokenizer: ${ModelScanner.formatFileSize(model.tokenizerSizeBytes)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(model.ptePath.absolutePath, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (model == uiState.selectedModel) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }

            Button(
                onClick = { viewModel.loadSelectedModel() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = uiState.selectedModel != null && uiState.engineState !is EngineLoadState.Loading
            ) { Text("Load Model & Start Chat") }

            OutlinedButton(onClick = { viewModel.scanForModels() }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Rescan /data/local/tmp/llama/")
            }
        }
    }
}
