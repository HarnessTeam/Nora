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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ai.nora.ui.chat.ChatScreen
import ai.nora.ui.chat.ChatViewModel
import ai.nora.ui.setup.SetupScreen
import ai.nora.ui.setup.SetupViewModel

@Composable
fun MainNavigation() {
    val preferencesManager = remember { NoraApp.instance.preferencesManager }

    // nullable Boolean: null = 尚未读取（默认 Chat，假设用户有历史对话），
    // true = 模型已加载 → Chat，false = 首次使用 → Setup
    val modelLoaded by preferencesManager.modelLoaded.collectAsStateWithLifecycle(
        lifecycleOwner = LocalLifecycleOwner.current,
        initialValue = null as Boolean?
    )

    // 导航默认目的地：modelLoaded == false → Setup；其余情况（null 或 true）→ Chat
    val initialDestination = if (modelLoaded == false) Setup else Chat

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
                    viewModel = remember { SetupViewModel(scanner, engine, preferencesManager) },
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
