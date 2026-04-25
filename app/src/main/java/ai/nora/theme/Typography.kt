package ai.nora.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Nora Typography — Constitution 3.3 字体规范
 * 使用 FontFamily.Default 作为系统字体加载入口：
 * - Android 系统会根据字体名（Inter / JetBrains Mono）自动加载对应字体文件
 * - 中文 fallback 由系统字体引擎处理（思源黑体优先级最高）
 * 基于 nora-design-system.md Section 4（Apple SF Pro 策略）
 */
private val Inter = FontFamily.Default
private val JetBrainsMono = FontFamily.Monospace

val NoraTypography = Typography(
    // ── Apple HIG 层次标题 ──
    headlineLarge   = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 29.sp),  // 大标题
    headlineMedium  = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp),
    headlineSmall   = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 23.sp),  // 欢迎语

    titleLarge      = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Bold,     fontSize = 20.sp, lineHeight = 26.sp),  // TopAppBar 标题
    titleMedium     = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),  // 卡片标题
    titleSmall      = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium,    fontSize = 14.sp, lineHeight = 20.sp),  // 小标题

    // ── 正文内容 ──
    bodyLarge    = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),  // 正文
    bodyMedium   = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),  // 辅助正文
    bodySmall    = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 17.sp),  // 时间戳、说明

    // ── 标签/按钮 ──
    labelLarge   = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),  // 按钮文字
    labelMedium  = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 17.sp),  // Tag、Badge
    labelSmall   = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp),  // 小标签
)

/** 代码展示专用 — JetBrains Mono */
val CodeTypography = TextStyle(fontFamily = JetBrainsMono, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 21.sp)
