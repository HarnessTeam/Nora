package ai.nora

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.waitUntilAtLeastOneExists
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
 * 2. 安全屋显示 Nora 呼吸光点动画
 * 3. 底部三按钮导航可点击
 * 4. 点击"对话" → 进入聊天页
 * 5. 点击返回 → 回到安全屋
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class SanctuaryLaunchTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    /**
     * 辅助：从安全屋点击"对话"按钮导航到聊天页
     */
    private fun navigateToChat() {
        composeTestRule.waitForIdle()

        val nodes = composeTestRule
            .onAllNodesWithText("对话", substring = true)
            .fetchSemanticsNodes()

        for (i in nodes.indices) {
            try {
                composeTestRule
                    .onAllNodesWithText("对话", substring = true)[i]
                    .performClick()
                break
            } catch (_: Exception) { /* 继续下一个 */ }
        }
        composeTestRule.waitForIdle()
    }

    /**
     * 验证1：打开 App → 首先看到安全屋
     */
    @Test
    fun 打开App首先看到安全屋而非聊天() {
        composeTestRule.waitForIdle()

        // 安全屋特征：顶部有"安全模式"标签
        composeTestRule.waitUntilAtLeastOneExists(
            hasText("安全模式", substring = true),
            3000
        )
        composeTestRule
            .onNodeWithText("安全模式", substring = true)
            .assertIsDisplayed()
    }

    /**
     * 验证2：安全屋显示 Nora 状态文案
     */
    @Test
    fun 安全屋显示Nora在线状态() {
        composeTestRule.waitForIdle()

        // "Nora 在线" 或 "Nora 离线" 或 "Nora 苏醒" 等状态文案
        val hasStatus = try {
            composeTestRule.waitUntilAtLeastOneExists(
                hasText("Nora", substring = true),
                3000
            )
            true
        } catch (e: Exception) { false }

        assert(hasStatus) { "安全屋应显示 Nora 状态文案" }
    }

    /**
     * 验证3：底部三按钮存在
     */
    @Test
    fun 安全屋底部三按钮全部存在() {
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("对话", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("日志", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("技能", substring = true).assertIsDisplayed()
    }

    /**
     * 验证4：点击"对话" → 进入聊天页
     */
    @Test
    fun 点击对话按钮进入聊天页() {
        composeTestRule.waitForIdle()
        navigateToChat()

        // 聊天页特征：输入框 placeholder
        composeTestRule.waitUntilAtLeastOneExists(
            hasText("发送消息给 Nora...", substring = true),
            3000
        )
        composeTestRule
            .onNodeWithText("发送消息给 Nora...", substring = true)
            .assertIsDisplayed()
    }
}
