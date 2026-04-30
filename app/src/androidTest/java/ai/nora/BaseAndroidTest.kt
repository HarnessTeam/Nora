package ai.nora

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
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
     * 2. 遍历所有可点击节点，找到底部"对话"按钮并点击
     * 3. 等待导航完成（ChatScreen 输入框出现）
     */
    @OptIn(ExperimentalTestApi::class)
    protected fun navigateToChatFromSanctuary() {
        composeTestRule.waitForIdle()
        Thread.sleep(1000) // 额外等待让动画稳定

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
                Thread.sleep(1000)
                return
            } catch (_: Exception) { /* 继续下一个 */ }
        }
    }
}
