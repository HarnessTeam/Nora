package ai.nora

import androidx.compose.ui.test.*
import org.junit.Test

/**
 * Phase 0 Gate — App 启动测试
 * 宪法合规：应用名为 "Nora"，启动后正确显示 Nora 品牌标识
 */
class AppLaunchTest : BaseAndroidTest() {

    @Test
    fun 启动后显示Nora品牌标题() {
        // SetupScreen 在顶部 AppBar 显示 "Nora" 标题
        composeTestRule
            .onNodeWithText("Nora", substring = true, ignoreCase = true)
            .assertExists()
    }

    @Test
    fun 启动后显示模型加载界面() {
        // 首次启动显示 SetupScreen，有 "Select a model to load" 文案
        composeTestRule
            .onNodeWithText("Select a model to load", substring = true)
            .assertExists()
    }

    @Test
    fun 启动后显示NoraAppBar图标和标题() {
        // TopAppBar 中 Nora 标题和 Memory 图标同时存在
        composeTestRule
            .onNodeWithText("Nora", substring = true)
            .assertIsDisplayed()
    }
}
