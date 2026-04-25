package ai.nora.theme

import androidx.compose.ui.graphics.Color

// Nora 宪法色彩系统 — 完全离线，零依赖 Material 紫色
// 铁律：Nora橙 #FF6B6B 是界面的唯一暖色，不可滥用

object NoraColors {
    // 背景层
    val Background   = Color(0xFF121212)  // 主背景
    val Surface      = Color(0xFF1E1E1E)  // 卡片/输入框/Tab栏
    val SurfaceEdge  = Color(0xFF2C2C2C)  // 气泡描边、分割线

    // 强调色
    val NoraOrange   = Color(0xFFFF6B6B)  // 呼吸灯/Nora头像/激活态/关键按钮

    // 文字
    val TextPrimary  = Color(0xFFE0E0E0)  // 正文
    val TextSecondary= Color(0xFF9E9E9E)  // 时间戳/思考过程

    // 可选黑客模式
    val MatrixGreen  = Color(0xFF00FF41)
}
