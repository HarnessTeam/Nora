package ai.nora.ui.setup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import ai.nora.ui.design.NoraCheckCircle
import ai.nora.ui.design.NoraFolder
import ai.nora.ui.design.NoraMemory
import ai.nora.ui.design.NoraRefresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ai.nora.llm.EngineLoadState
import ai.nora.model.ModelScanner

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
                        Icon(
                            imageVector = NoraMemory,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Nora", fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.scanForModels() }) {
                        Icon(
                            imageVector = NoraRefresh,
                            contentDescription = "刷新"
                        )
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

            // 内置模型提取进度（bundled flavor 首次启动）
            AnimatedVisibility(visible = uiState.isExtractingBundledModel) {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(12.dp))
                            Text("正在准备内置模型...", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        }
                        uiState.extractionProgress?.let { progress ->
                            Spacer(Modifier.height(8.dp))
                            Text(progress, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                        }
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
                Spacer(Modifier.height(8.dp))
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
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(model.name, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                                    // 模型来源标签
                                    SourceTag(source = model.source)
                                }
                                Text("Model: ${model.displaySize} | Tokenizer: ${ModelScanner.formatFileSize(model.tokenizerSizeBytes)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(model.ptePath.absolutePath, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (model == uiState.selectedModel) {
                                Icon(
                                    imageVector = NoraCheckCircle,
                                    contentDescription = "已选择",
                                    tint = MaterialTheme.colorScheme.primary
                                )
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
                Icon(
                    imageVector = NoraFolder,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Rescan /data/local/tmp/llama/")
            }
        }
    }
}

/**
 * 模型来源标签 — 区分内置模型 / ADB 推送 / 下载目录
 */
@Composable
private fun SourceTag(source: ModelScanner.ModelSource) {
    val (label, color) = when (source) {
        ModelScanner.ModelSource.BUNDLED -> "内置" to MaterialTheme.colorScheme.primary
        ModelScanner.ModelSource.ADB -> "ADB" to MaterialTheme.colorScheme.tertiary
        ModelScanner.ModelSource.DOWNLOAD -> "下载" to MaterialTheme.colorScheme.secondary
    }

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}
