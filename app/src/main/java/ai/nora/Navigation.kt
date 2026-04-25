package ai.nora

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import ai.nora.llm.ExecuTorchEngine
import ai.nora.model.ModelAssetManager
import ai.nora.model.ModelScanner
import ai.nora.ui.chat.ChatScreen
import ai.nora.ui.chat.ChatViewModel
import kotlinx.coroutines.launch

/**
 * Nora 主导航 — 直接进入 Chat，模型在后台自动加载。
 *
 * 流程：
 * 1. 应用启动 → MainNavigation 初始化
 * 2. ExecuTorchEngine + ModelScanner 实例化（remember，跨 recomposition 保持）
 * 3. LaunchedEffect + coroutineScope 触发模型加载流程
 * 4. ChatViewModel 监听 engine.loadState → isModelReady 变化 → UI 更新
 * 5. 模型加载完成前：ChatScreen 显示全屏加载覆盖层
 */
@Composable
fun MainNavigation() {
    val app = NoraApp.instance

    // Shared engine and scanner (manual DI, survive recomposition)
    val engine = remember { ExecuTorchEngine(app) }
    val scanner = remember { ModelScanner(app) }
    val dataRepository = remember { app.dataRepository }

    val scope = rememberCoroutineScope()

    // 启动模型加载流程（bundled：提取+扫描+加载；slim：仅扫描）
    LaunchedEffect(Unit) {
        scope.launch {
            loadBundledModelIfNeeded(engine, scanner)
        }
    }

    // Shared backstack — 始终直接进入 Chat（无需 Setup 页面）
    val backStack = rememberNavBackStack(Chat)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<Chat> {
                ChatScreen(
                    viewModel = remember { ChatViewModel(engine, dataRepository) },
                    onUnloadModel = { engine.unload() }
                )
            }
        }
    )
}

/**
 * 加载内置模型（Bundled 变体）或外部模型（Slim 变体）。
 * bundled：提取 → 扫描 → 选中内置模型 → 加载
 * slim：扫描外部路径 → 加载第一个可用模型
 */
private suspend fun loadBundledModelIfNeeded(
    engine: ExecuTorchEngine,
    scanner: ModelScanner
) {
    val context = engine.context

    if (BuildConfig.MODEL_BUNDLED) {
        // 已有提取好的模型 → 直接扫描并加载
        val extracted = ModelAssetManager.getExtractedModelPath(context)
        if (extracted != null && !ModelAssetManager.needsReExtraction(context)) {
            scanAndLoadBundled(engine, scanner, extracted)
            return
        }

        // 需要提取（带 callback 回调）
        ModelAssetManager.extractBundledModel(
            context,
            progress = object : ModelAssetManager.ExtractionProgress {
                override fun onProgress(current: Int, total: Int, filename: String) {
                    // 无需更新 UI，ChatScreen 的 engineState 监听会反映 Loading 状态
                }

                override fun onComplete(modelPath: String, tokenizerPath: String) {
                    // 提取成功 → 扫描并加载（同步调用，因为 onComplete 已在 IO 完成）
                }

                override fun onError(message: String, throwable: Throwable?) {
                    // 提取失败 → 降级到外部扫描
                }
            }
        )
        // extractBundledModel 返回后检查是否提取成功
        val afterExtract = ModelAssetManager.getExtractedModelPath(context)
        if (afterExtract != null) {
            scanAndLoadBundled(engine, scanner, afterExtract)
        } else {
            // 提取失败，尝试外部扫描
            scanAndLoadFirst(engine, scanner)
        }
    } else {
        // slim flavor：直接扫描外部路径
        scanAndLoadFirst(engine, scanner)
    }
}

/**
 * 扫描所有模型，确保内置模型优先并加载。
 */
private suspend fun scanAndLoadBundled(
    engine: ExecuTorchEngine,
    scanner: ModelScanner,
    bundledPath: String
) {
    val models = scanner.scanModels()
    val sorted = models.sortedByDescending { it.ptePath.absolutePath == bundledPath }
    val selected = sorted.firstOrNull { it.ptePath.absolutePath == bundledPath }
        ?: sorted.firstOrNull()

    selected?.let {
        engine.loadModel(
            modelPath = it.ptePath.absolutePath,
            tokenizerPath = it.tokenizerPath.absolutePath
        )
    }
}

/**
 * 扫描外部模型，找到第一个可用并加载。
 */
private suspend fun scanAndLoadFirst(
    engine: ExecuTorchEngine,
    scanner: ModelScanner
) {
    val models = scanner.scanModels()
    models.firstOrNull()?.let {
        engine.loadModel(
            modelPath = it.ptePath.absolutePath,
            tokenizerPath = it.tokenizerPath.absolutePath
        )
    }
}
