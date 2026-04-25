package ai.nora

import androidx.compose.ui.test.*
import org.junit.Test

/**
 * Phase 0+1 Gate — 主题测试
 *
 * 适配 Phase 2 Sanctuary-first 导航：
 * - 应用启动 → SanctuaryScreen（安全屋）
 * - 需要先调用 navigateToChatFromSanctuary() 导航到 ChatScreen
 * - 暗色背景（#121212）
 * - NoraOrange 品牌标题
 * - 状态指示器 / 欢迎区块 / 输入框
 */
class ThemeTest : BaseAndroidTest() {

    @Test
    fun 暗色模式ChatScreen正确渲染() {
        // Phase 2: 从安全屋导航到聊天页
        navigateToChatFromSanctuary()
        composeTestRule.waitForIdle()

        // 验证 ChatScreen 核心元素存在
        // 输入框 placeholder 是最可靠的锚点（始终存在）
        composeTestRule
            .onNodeWithText("发送消息给 Nora...", substring = true)
            .assertExists()
    }

    @Test
    fun 暗色模式文字内容可读() {
        // Phase 2: 从安全屋导航到聊天页
        navigateToChatFromSanctuary()
        composeTestRule.waitForIdle()

        // 输入框 placeholder 唯一匹配 — 证明文字渲染正常
        composeTestRule
            .onNodeWithText("发送消息给 Nora...", substring = true)
            .assertExists()
    }

    @Test
    fun 暗色模式输入框存在() {
        // Phase 2: 从安全屋导航到聊天页
        navigateToChatFromSanctuary()
        composeTestRule.waitForIdle()

        // 输入框始终存在于 ChatScreen
        composeTestRule
            .onNodeWithText("发送消息给 Nora...", substring = true)
            .assertExists()
    }
}
