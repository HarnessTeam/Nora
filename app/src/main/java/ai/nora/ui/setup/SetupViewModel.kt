package ai.nora.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ai.nora.BuildConfig
import ai.nora.data.PreferencesManager
import ai.nora.llm.EngineLoadState
import ai.nora.llm.ExecuTorchEngine
import ai.nora.model.ModelAssetManager
import ai.nora.model.ModelScanner
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
    val error: String? = null,
    /** 是否正在提取内置模型（bundled flavor 首次启动） */
    val isExtractingBundledModel: Boolean = false,
    /** 提取内置模型进度消息 */
    val extractionProgress: String? = null
)

class SetupViewModel(
    private val modelScanner: ModelScanner,
    private val llmEngine: ExecuTorchEngine,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SetupUiState())
    val uiState: StateFlow<SetupUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            llmEngine.loadState.collect { state ->
                _uiState.value = _uiState.value.copy(engineState = state)
                if (state is EngineLoadState.Loaded) {
                    _uiState.value = _uiState.value.copy(loadComplete = true)
                    // 持久化：标记模型已加载，下次启动直接进入 Chat
                    preferencesManager.setModelLoaded()
                }
                if (state is EngineLoadState.Error) {
                    _uiState.value = _uiState.value.copy(error = state.message)
                }
            }
        }

        // bundled flavor：优先尝试提取内置模型
        if (BuildConfig.MODEL_BUNDLED) {
            extractAndSelectBundledModel()
        } else {
            scanForModels()
        }
    }

    /**
     * bundled flavor 专用：提取内置模型 → 自动选中 → 扫描外部路径补全
     *
     * 流程：
     * 1. 检查是否已提取（ModelAssetManager 幂等）
     * 2. 如果需要提取，显示进度
     * 3. 提取成功后自动选中内置模型
     * 4. 扫描外部路径补全列表
     * 5. 自动加载模型到引擎
     */
    private fun extractAndSelectBundledModel() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isExtractingBundledModel = true,
                extractionProgress = "准备内置模型..."
            )

            // 检查是否已有提取好的模型
            val appContext = llmEngine.context
            val extractedPath = ModelAssetManager.getExtractedModelPath(appContext)
            if (extractedPath != null && !ModelAssetManager.needsReExtraction(appContext)) {
                _uiState.value = _uiState.value.copy(
                    isExtractingBundledModel = false,
                    extractionProgress = null
                )
                // 已有提取结果，直接扫描并优先选中内置模型，然后自动加载
                scanForModelsWithBundledPriorityAndLoad(extractedPath)
                return@launch
            }

            // 需要提取
            val result = ModelAssetManager.extractBundledModel(
                appContext,
                progress = object : ai.nora.model.ModelAssetManager.ExtractionProgress {
                    override fun onProgress(current: Int, total: Int, filename: String) {
                        _uiState.value = _uiState.value.copy(
                            extractionProgress = "提取中: $filename ($current/$total)"
                        )
                    }

                    override fun onComplete(extractedModelPath: String, extractedTokenizerPath: String) {
                        _uiState.value = _uiState.value.copy(
                            isExtractingBundledModel = false,
                            extractionProgress = null
                        )
                        // 提取成功后，扫描并优先选中内置模型，然后自动加载
                        scanForModelsWithBundledPriorityAndLoad(extractedModelPath)
                    }

                    override fun onError(message: String, throwable: Throwable?) {
                        _uiState.value = _uiState.value.copy(
                            isExtractingBundledModel = false,
                            extractionProgress = null,
                            error = message
                        )
                        // 提取失败时降级：继续扫描外部路径
                        scanForModels()
                    }
                }
            )
        }
    }

    /**
     * 扫描所有模型，并确保内置模型排在第一位，然后自动加载。
     * 用于 bundled flavor：提取成功后自动完成扫描 → 选中 → 加载全流程。
     */
    private fun scanForModelsWithBundledPriorityAndLoad(bundledModelPath: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isScanning = true, error = null)
            try {
                val models = modelScanner.scanModels()
                // 确保内置模型排在最前
                val sortedModels = models.sortedByDescending { it.ptePath.absolutePath == bundledModelPath }
                val selected = sortedModels.firstOrNull { it.ptePath.absolutePath == bundledModelPath }
                    ?: sortedModels.firstOrNull()
                _uiState.value = _uiState.value.copy(
                    models = sortedModels,
                    selectedModel = selected,
                    isScanning = false
                )
                // 自动加载选中的模型
                selected?.let { loadSelectedModel(it) }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isScanning = false,
                    error = "Scan failed: ${e.message}"
                )
            }
        }
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
                    error = if (models.isEmpty() && !BuildConfig.MODEL_BUNDLED)
                        "No model files found.\n\n" +
                        "Please push your model via ADB:\n" +
                        "  adb push model.pte /data/local/tmp/llama/\n" +
                        "  adb push tokenizer.json /data/local/tmp/llama/\n" +
                        "Then tap Rescan above."
                    else if (models.isEmpty())
                        "No model found. Please place model.pte in /data/local/tmp/llama/"
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
        loadSelectedModel(model)
    }

    /**
     * 加载指定模型到引擎。
     */
    private fun loadSelectedModel(model: ModelScanner.ModelFile) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(error = null)
            llmEngine.loadModel(
                modelPath = model.ptePath.absolutePath,
                tokenizerPath = model.tokenizerPath.absolutePath
            )
        }
    }
}
