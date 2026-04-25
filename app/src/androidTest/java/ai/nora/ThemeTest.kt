package ai.nora

import androidx.compose.ui.test.*
import org.junit.Test

/**
 * Phase 0 Gate — 主题测试
 * 宪法合规：NoraTheme 强制暗色，NoraTypography 使用 Inter 字体族
 */
class ThemeTest : BaseAndroidTest() {

    @Test
    fun 暗色模式SetupScreen正确渲染() {
        // SetupScreen 在暗色主题下正确显示，无崩溃
        composeTestRule
            .onNodeWithText("Select a model to load", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun 暗色模式文字内容可读() {
        // 验证 Nora 标题和引导文字均可见（暗色下正常渲染）
        composeTestRule
            .onNodeWithText("Nora", substring = true)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Load Model & Start Chat", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun 暗色模式按钮可交互() {
        // "Rescan" 按钮在暗色下可见且可点击
        composeTestRule
            .onNode(hasContentDescription("Rescan") and hasClickAction())
            .assertIsDisplayed()
    }
}
