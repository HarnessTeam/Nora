package ai.nora

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ai.nora.ui.chat.ChatScreen
import ai.nora.ui.chat.ChatViewModel
import ai.nora.ui.setup.SetupScreen
import ai.nora.ui.setup.SetupViewModel
import kotlinx.coroutines.flow.first

@Composable
fun MainNavigation() {
    // 从 DataStore 读取 modelLoaded 状态（持久化，跨 Session 生效）
    val preferencesManager = remember { NoraApp.instance.preferencesManager }
    val modelLoadedFlow by preferencesManager.modelLoaded.collectAsStateWithLifecycle(
        lifecycleOwner = LocalLifecycleOwner.current,
        initialValue = false
    )
    // 导航默认目的地：DataStore 中已加载过模型 → Chat，否则 → Setup
    val initialDestination = if (modelLoadedFlow) Chat else Setup

    val backStack = rememberNavBackStack(initialDestination)

    // Shared engine and scanner instances (manual DI)
    val app = NoraApp.instance
    val engine = remember { ai.nora.llm.ExecuTorchEngine(app) }
    val scanner = remember { ai.nora.model.ModelScanner(app) }

    // Data layer injection from Application
    val dataRepository = remember { app.dataRepository }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<Setup> {
                SetupScreen(
                    viewModel = remember { SetupViewModel(scanner, engine) },
                    onModelLoaded = {
                        backStack.clear()
                        backStack.add(Chat)
                    }
                )
            }
            entry<Chat> {
                ChatScreen(
                    viewModel = remember { ChatViewModel(engine, dataRepository) },
                    onUnloadModel = {
                        backStack.clear()
                        backStack.add(Setup)
                    }
                )
            }
        }
    )
}
