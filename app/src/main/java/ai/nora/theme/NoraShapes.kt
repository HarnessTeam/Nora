package ai.nora.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Nora Shapes — Apple HIG 圆角规范
// 基于 nora-design-system.md Section 5
//
// 圆角原则（Apple HIG）：
// - 越大越友好（24dp 胶囊 → 无攻击感）
// - 功能控件用小圆角（8dp-12dp）
// - 内容卡片用大圆角（16dp-20dp）

object NoraShapes {
    // ═══════════════════════════════════════════
    // 核心圆角常量
    // ═══════════════════════════════════════════

    /** 输入框 — 24dp 大圆角胶囊（Apple 一贯风格） */
    val InputBar = 24.dp

    /** 消息气泡 — 16dp（左侧下圆角给用户，右侧下圆角给助手） */
    val MessageBubble = 16.dp

    /** 快捷功能卡片 — 16dp */
    val QuickActionCard = 16.dp

    /** 按钮 — 12dp */
    val Button = 12.dp

    /** 小型控件（Tag、Badge） — 8dp */
    val Tag = 8.dp

    /** 状态指示器 — 全圆（CircleShape） */
    // 使用 CircleShape，不要用 dp

    // ═══════════════════════════════════════════
    // Compose Shapes（供 MaterialTheme 使用）
    // ═══════════════════════════════════════════

    // Shapes 接受 RoundedCornerShape 对象
    val NoraShapes = Shapes(
        extraSmall = RoundedCornerShape(4.dp),
        small = RoundedCornerShape(8.dp),
        medium = RoundedCornerShape(16.dp),
        large = RoundedCornerShape(24.dp),
        extraLarge = RoundedCornerShape(28.dp)
    )

    // ═══════════════════════════════════════════
    // 快捷 Shape 引用（避免每次 new RoundedCornerShape）
    // ═══════════════════════════════════════════

    val InputBarShape = RoundedCornerShape(24.dp)
    val MessageBubbleShape = RoundedCornerShape(16.dp)
    val QuickActionCardShape = RoundedCornerShape(16.dp)
    val ButtonShape = RoundedCornerShape(12.dp)
    val TagShape = RoundedCornerShape(8.dp)
}
