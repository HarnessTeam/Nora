package ai.nora.ui.log

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.nora.theme.NoraColors
import ai.nora.theme.NoraShapes

// ═══════════════════════════════════════════════════════════════
// LogScreen — 感知日志页（宪法 3.4.B + Phase 6 感知维度）
// 底部三按钮 → 日志：展示 Nora「感知」到的系统事件
// ═══════════════════════════════════════════════════════════════

private data class SenseLogEntry(
    val icon: String,
    val title: String,
    val description: String,
    val time: String,
    val type: SenseType
)

private enum class SenseType { NOTIFICATION, FILE, MEMORY, SYSTEM }

@Composable
fun LogScreen(
    modifier: Modifier = Modifier
) {
    // 模拟感知日志数据（Phase 6 正式接入 NotificationListenerService）
    val senseLogs = listOf(
        SenseLogEntry("🔔", "通知感知", "已监听 3 个应用通知", "刚刚", SenseType.NOTIFICATION),
        SenseLogEntry("📄", "文件上下文", "已接入 0 个文件", "—", SenseType.FILE),
        SenseLogEntry("🧠", "对话记忆", "已保存 12 条对话", "今天", SenseType.MEMORY),
        SenseLogEntry("⚙️", "系统状态", "Nora 正在本地运行", "在线", SenseType.SYSTEM),
    )

    Scaffold(
        containerColor = NoraColors.Background
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(24.dp))

            // ── 页面标题 ──
            Text(
                text = "Nora 的感知",
                style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = NoraColors.PrimaryText
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "你的数据，只有 Nora 见过",
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                color = NoraColors.SecondaryText
            )

            Spacer(Modifier.height(24.dp))

            // ── 安全承诺卡片 ──
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = NoraShapes.QuickActionCardShape,
                color = NoraColors.SurfaceElevated
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(NoraColors.NoraOrange.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🔒", fontSize = 18.sp)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "完全本地 · 零网络请求",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = NoraColors.PrimaryText
                        )
                        Text(
                            text = "所有感知数据仅存储在您的设备中",
                            fontSize = 12.sp,
                            color = NoraColors.SecondaryText
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── 感知条目列表 ──
            senseLogs.forEach { entry ->
                SenseLogCard(entry = entry)
                Spacer(Modifier.height(12.dp))
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SenseLogCard(entry: SenseLogEntry) {
    val iconBgColor = when (entry.type) {
        SenseType.NOTIFICATION -> NoraColors.NoraThinking.copy(alpha = 0.15f)
        SenseType.FILE -> NoraColors.NoraReady.copy(alpha = 0.15f)
        SenseType.MEMORY -> NoraColors.NoraOrange.copy(alpha = 0.15f)
        SenseType.SYSTEM -> NoraColors.Surface.copy(alpha = 0.5f)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = NoraColors.Surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Text(entry.icon, fontSize = 18.sp)
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = NoraColors.PrimaryText
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = entry.description,
                    fontSize = 12.sp,
                    color = NoraColors.SecondaryText
                )
            }

            Text(
                text = entry.time,
                fontSize = 11.sp,
                color = NoraColors.TertiaryText
            )
        }
    }
}
