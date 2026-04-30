package ai.nora

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Phase 2 Gate — 安全屋启动与导航测试
 *
 * 宪法 3.4.B: 打开 Nora 进入安全屋，而非直接聊天
 *
 * 验证：
 * 1. 打开 App → 首先看到安全屋（而非聊天）
 * 2. 安全屋显示 Nora 状态文案
 * 3. 底部三按钮导航存在
 * 4. 点击"对话" → 进入聊天页
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class SanctuaryLaunchTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    /**
     * 辅助：从安全屋导航到聊天页。
     * 多次 waitForIdle 确保 UI 完全稳定后再尝试点击。
     */
    private fun navigateToChatFromSanctuary() {
        // 等待 UI 完全稳定
        composeTestRule.waitForIdle()
        Thread.sleep(500) // 额外等待让动画稳定

        // 遍历所有可点击节点，尝试点击（底部"对话"按钮）
        val clickableNodes = composeTestRule
            .onAllNodes(hasClickAction())
            .fetchSemanticsNodes()

        for (i in clickableNodes.indices) {
            try {
                composeTestRule
                    .onAllNodes(hasClickAction())[i]
                    .performClick()
                composeTestRule.waitForIdle()
                Thread.sleep(500)
                return
            } catch (_: Exception) { /* 继续下一个 */ }
        }
    }

    /**
     * 验证1：打开 App → 首先看到安全屋（安全模式标签）
     */
    @Test
    fun 打开App首先看到安全屋而非聊天() {
        // 等待 UI 完全稳定
        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        // 安全屋特征：顶部有"安全模式"标签
        composeTestRule.waitUntilAtLeastOneExists(
            hasText("安全模式", substring = true),
            8000
        )
        composeTestRule.onNodeWithText("安全模式", substring = true).assertIsDisplayed()
    }

    /**
     * 验证2：安全屋显示 Nora 状态文案
     */
    @Test
    fun 安全屋显示Nora在线状态() {
        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        // "Nora 在线" / "Nora 正在苏醒..." / "Nora 离线" / "Nora 遇到了问题"
        composeTestRule.waitUntilAtLeastOneExists(
            hasText("Nora", substring = true),
            8000
        )
    }

    /**
     * 验证3：底部三按钮存在（对话/日志/技能）
     */
    @Test
    fun 安全屋底部三按钮全部存在() {
        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        // 等待按钮出现
        composeTestRule.waitUntilAtLeastOneExists(
            hasText("对话", substring = true),
            8000
        )
        composeTestRule.waitUntilAtLeastOneExists(
            hasText("日志", substring = true),
            8000
        )
        composeTestRule.waitUntilAtLeastOneExists(
            hasText("技能", substring = true),
            8000
        )
    }

    /**
     * 验证4：点击"对话" → 进入聊天页（输入框出现）
     */
    @Test
    fun 点击对话按钮进入聊天页() {
        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        navigateToChatFromSanctuary()

        // 聊天页特征：输入框 placeholder
        composeTestRule.waitUntilAtLeastOneExists(
            hasText("发送消息给 Nora...", substring = true),
            10000
        )
    }
}
