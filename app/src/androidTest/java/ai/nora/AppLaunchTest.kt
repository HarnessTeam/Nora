package ai.nora

import androidx.compose.ui.test.*
import org.junit.Test

/**
 * Phase 0+1 Gate — App 启动测试（适配 ChatScreen 首页导航）
 *
 * 当前 UI 流：
 * - 应用启动 → 直接进入 ChatScreen（Navigation.kt 已改为 rememberNavBackStack(Chat)）
 * - TopAppBar 显示 "Nora" + Logo + 状态指示器
 * - 无模型时：加载覆盖层 "正在准备 Nora..."
 * - 有模型时：WelcomeSection（"欢迎使用 Nora" / "欢迎回来"）+ 输入框
 */
class AppLaunchTest : BaseAndroidTest() {

    @Test
    fun 启动后显示Nora品牌标题() {
        composeTestRule.waitForIdle()

        // Nora 品牌在多个位置出现，证明品牌渲染正确
        val nodes = composeTestRule
            .onAllNodesWithText("Nora", substring = true)
            .fetchSemanticsNodes()

        assert(nodes.isNotEmpty()) { "应存在包含 'Nora' 的 UI 节点" }
    }

    @Test
    fun 启动后显示NoraTopBar区域() {
        composeTestRule.waitForIdle()

        // 验证 "Nora" 文字至少出现在 1 个节点上
        // fetchSemanticsNodes 返回所有匹配节点（包括不可见的）
        val noraNodes = composeTestRule
            .onAllNodesWithText("Nora", substring = true)
            .fetchSemanticsNodes()

        assert(noraNodes.size >= 1) { "TopBar 区域应包含 'Nora' 标题" }
    }

    @Test
    fun 启动后显示聊天界面或加载状态() {
        composeTestRule.waitForIdle()

        // 二选一：加载覆盖层 或 欢迎区块
        val isLoading = try {
            composeTestRule.onNodeWithText("正在准备", substring = true).assertExists()
            true
        } catch (e: Exception) { false }

        val isWelcome = try {
            composeTestRule.onNodeWithText("欢迎", substring = true).assertExists()
            true
        } catch (e: Exception) { false }

        assert(isLoading || isWelcome) { "应显示加载状态或欢迎区块" }
    }
}
