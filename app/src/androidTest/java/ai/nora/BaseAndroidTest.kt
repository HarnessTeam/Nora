package ai.nora

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import ai.nora.MainActivity
import org.junit.Rule
import org.junit.runner.RunWith

/**
 * Nora 测试基类 — 所有 Instrument 测试共享的基础设施。
 *
 * 提供通用的 Compose 测试规则（基于 MainActivity），
 * 后续 Phase 可在此处添加 Hilt 注入、测试数据库等共享设施。
 *
 * 导航辅助（Phase 2 Sanctuary-first）：
 * - navigateToChatFromSanctuary()：从安全屋导航到聊天页
 */
@RunWith(AndroidJUnit4::class)
abstract class BaseAndroidTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    /**
     * Phase 2 导航辅助：从 Sanctuary（安全屋）导航到 ChatScreen（聊天页）。
     *
     * 背景：Phase 2 开始，应用首个页面改为 SanctuaryScreen（安全屋）。
     * 需要进入 ChatScreen 的测试，先调用此函数完成导航。
     *
     * 流程：
     * 1. 等待界面稳定
     * 2. 查找并点击"对话"按钮
     * 3. 等待导航完成（ChatScreen 输入框出现）
     */
    @OptIn(ExperimentalTestApi::class)
    protected fun navigateToChatFromSanctuary() {
        composeTestRule.waitForIdle()

        // 点击"对话"按钮（底部导航）
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

        // 等待 ChatScreen 加载（输入框出现）
        composeTestRule.waitForIdle()
    }
}
