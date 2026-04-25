package ai.nora.theme

import androidx.compose.ui.graphics.Color

// Nora 宪法色彩系统 — 完全离线，零依赖 Material 紫色
// 铁律：NoraOrange #FF6B6B 是界面的唯一暖色，不可滥用
// 基于 nora-design-system.md Apple HIG 对齐版

object NoraColors {
    // ═══════════════════════════════════════════
    // 品牌色系 — NoraOrange 核心
    // ═══════════════════════════════════════════
    val NoraOrange        = Color(0xFFFF6B6B)  // 品牌主色：按钮、高亮、用户消息
    val NoraOrangeLight    = Color(0xFFFF8A8A)  // 悬停态、次要强调
    val NoraOrangeDark    = Color(0xFFE85555)  // 按压态、深度强调

    // ═══════════════════════════════════════════
    // 背景层 — Apple 纯黑策略
    // ═══════════════════════════════════════════
    val Background        = Color(0xFF121212)  // 全局主背景
    val Surface           = Color(0xFF1E1E1E)  // 卡片、输入框、对话框
    val SurfaceElevated   = Color(0xFF2A2A2A)  // 浮层、BottomSheet、浮起元素
    val Divider           = Color(0xFF2C2C2C)  // 分隔线、描边、边框

    // ═══════════════════════════════════════════
    // 文字色 — 清晰层次
    // ═══════════════════════════════════════════
    val PrimaryText       = Color(0xFFFFFFFF)  // 主要文字（正文标题）
    val SecondaryText     = Color(0xFF9E9E9E)  // 次要文字、时间戳、辅助说明
    val TertiaryText      = Color(0xFF666666)  // 禁用态、占位符

    // ═══════════════════════════════════════════
    // 状态色 — Apple 指示器风格
    // ═══════════════════════════════════════════
    val NoraReady         = Color(0xFF4ADE80)  // 就绪态（Apple 绿）
    val NoraThinking      = Color(0xFFFACC15)  // 思考中（Apple 黄）
    val NoraError         = Color(0xFFEF4444)  // 错误态（Apple 红）
    val NoraOffline       = Color(0xFF6B7280)  // 离线态

    // ═══════════════════════════════════════════
    // 可选黑客模式
    // ═══════════════════════════════════════════
    val MatrixGreen       = Color(0xFF00FF41)

    // ═══════════════════════════════════════════
    // 快捷操作卡片背景（MessageBubble 用户侧）
    // ═══════════════════════════════════════════
    val UserBubbleBg      = NoraOrange        // 用户消息气泡背景
    val UserBubbleText    = Color(0xFFFFFFFF)  // 用户消息文字
    val AssistantBubbleBg = Surface           // 助手消息气泡背景
    val AssistantBubbleBorder = Divider       // 助手消息描边
    val AssistantBubbleText = PrimaryText     // 助手消息文字
}
