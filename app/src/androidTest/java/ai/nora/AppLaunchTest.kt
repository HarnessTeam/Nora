package ai.nora

import androidx.compose.ui.test.*
import org.junit.Test

/**
 * Phase 0+1 Gate — App 启动测试
 *
 * 适配 Phase 2 Sanctuary-first 导航：
 * - 应用启动 → SanctuaryScreen（安全屋），而非直接进入 ChatScreen
 * - 安全屋显示："安全模式"标签 + Nora 状态文案（在线/正在苏醒/离线/问题）
 * - 无模型时：SanctuaryScreen 显示 "Nora 离线" 或 "Nora 正在苏醒..."
 * - 有模型时：SanctuaryScreen 显示 "Nora 在线" + NoraBreathingOrb 呼吸光点
 */
class AppLaunchTest : BaseAndroidTest() {

    @Test
    fun 启动后显示Nora品牌标题() {
        composeTestRule.waitForIdle()

        // Phase 2：SanctuaryScreen 安全屋中 "Nora" 出现在 2+ 个节点
        // （状态文案 + 苏醒日志等），不再要求恰好 1 个
        val nodes = composeTestRule
            .onAllNodesWithText("Nora", substring = true)
            .fetchSemanticsNodes()

        assert(nodes.size >= 1) { "应存在包含 'Nora' 的 UI 节点（Phase 2: 安全屋多节点）" }
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
    fun 启动后显示安全屋或聊天界面() {
        composeTestRule.waitForIdle()

        // Phase 2: 应用启动显示 SanctuaryScreen（安全屋）
        // 安全屋文案：Nora 在线 / Nora 正在苏醒... / Nora 离线 / Nora 遇到了问题
        val hasNoraStatus = try {
            composeTestRule.onNodeWithText("Nora", substring = true).assertExists()
            true
        } catch (e: Exception) { false }

        assert(hasNoraStatus) { "应显示 Nora 状态文案（安全屋）" }
    }
}
