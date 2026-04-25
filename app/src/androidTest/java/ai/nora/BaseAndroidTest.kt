package ai.nora

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
 */
@RunWith(AndroidJUnit4::class)
abstract class BaseAndroidTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()
}
