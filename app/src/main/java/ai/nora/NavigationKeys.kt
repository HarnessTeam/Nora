package ai.nora

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// ═══════════════════════════════════════════════════════
// 导航键 — 宪法 3.4.B
// ═══════════════════════════════════════════════════════

/** 安全屋首页 */
@Serializable
data object Sanctuary : NavKey

/** 对话页 */
@Serializable
data object Chat : NavKey

/** 日志页（Phase 4 实现） */
@Serializable
data object Log : NavKey

/** 技能树页（Phase 5 实现） */
@Serializable
data object Skill : NavKey
