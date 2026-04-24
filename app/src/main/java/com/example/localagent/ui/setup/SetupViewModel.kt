package com.example.localagent.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localagent.llm.EngineLoadState
import com.example.localagent.llm.ExecuTorchEngine
import com.example.localagent.model.ModelScanner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SetupUiState(
    val models: List<ModelScanner.ModelFile> = emptyList(),
    val selectedModel: ModelScanner.ModelFile? = null,
    val isScanning: Boolean = false,
    val engineState: EngineLoadState = EngineLoadState.Unloaded,
    val loadComplete: Boolean = false,
    val error: String? = null
)

class SetupViewModel(
    private val modelScanner: ModelScanner,
    private val llmEngine: ExecuTorchEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(SetupUiState())
    val uiState: StateFlow<SetupUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            llmEngine.loadState.collect { state ->
                _uiState.value = _uiState.value.copy(engineState = state)
                if (state is EngineLoadState.Loaded) {
                    _uiState.value = _uiState.value.copy(loadComplete = true)
                }
                if (state is EngineLoadState.Error) {
                    _uiState.value = _uiState.value.copy(error = state.message)
                }
            }
        }
        scanForModels()
    }

    fun scanForModels() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isScanning = true, error = null)
            try {
                val models = modelScanner.scanModels()
                _uiState.value = _uiState.value.copy(
                    models = models,
                    selectedModel = models.firstOrNull(),
                    isScanning = false,
                    error = if (models.isEmpty())
                        "No model files found.\n\nPlease run:\nadb push qwen3_0.6B_model.pte /data/local/tmp/llama/\nadb push tokenizer.json /data/local/tmp/llama/"
                    else null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isScanning = false, error = "Scan failed: ${e.message}")
            }
        }
    }

    fun selectModel(model: ModelScanner.ModelFile) {
        _uiState.value = _uiState.value.copy(selectedModel = model)
    }

    fun loadSelectedModel() {
        val model = _uiState.value.selectedModel ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(error = null)
            llmEngine.loadModel(
                modelPath = model.ptePath.absolutePath,
                tokenizerPath = model.tokenizerPath.absolutePath
            )
        }
    }
}
