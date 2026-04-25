package ai.nora

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import ai.nora.ui.chat.ChatScreen
import ai.nora.ui.chat.ChatViewModel
import ai.nora.ui.setup.SetupScreen
import ai.nora.ui.setup.SetupViewModel

@Composable
fun MainNavigation() {
    var modelLoaded by rememberSaveable { mutableStateOf(false) }
    val backStack = rememberNavBackStack(if (modelLoaded) Chat else Setup)

    // Shared engine and scanner instances (manual DI)
    val app = LocalAgentApp.instance
    val engine = remember { ai.nora.llm.ExecuTorchEngine(app) }
    val scanner = remember { ai.nora.model.ModelScanner(app) }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<Setup> {
                SetupScreen(
                    viewModel = remember { SetupViewModel(scanner, engine) },
                    onModelLoaded = {
                        modelLoaded = true
                        backStack.clear()
                        backStack.add(Chat)
                    }
                )
            }
            entry<Chat> {
                ChatScreen(
                    viewModel = remember { ChatViewModel(engine) },
                    onUnloadModel = {
                        modelLoaded = false
                        backStack.clear()
                        backStack.add(Setup)
                    }
                )
            }
        }
    )
}
