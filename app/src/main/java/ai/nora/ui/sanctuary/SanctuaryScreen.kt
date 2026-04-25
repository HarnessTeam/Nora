package ai.nora.ui.sanctuary

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.nora.llm.EngineLoadState
import ai.nora.theme.NoraColors
import ai.nora.theme.NoraShapes
import ai.nora.ui.chat.ChatViewModel
import ai.nora.ui.chat.ModelStatus
import ai.nora.ui.design.BreathingDot
import ai.nora.ui.design.NoraBreathingOrb
import ai.nora.ui.design.NoraStatus
import ai.nora.ui.design.NoraStatusIndicator

// ═══════════════════════════════════════════════════════
// SanctuaryScreen — 安全屋主界面
// 宪法 3.4.B: 打开 Nora 进入安全屋，而非直接聊天
// ═══════════════════════════════════════════════════════

@Composable
fun SanctuaryScreen(
    viewModel: ChatViewModel,
    onNavigateToChat: () -> Unit,
    onNavigateToLog: () -> Unit = {},
    onNavigateToSkill: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    // 从 engineState 推导 NoraStatus
    val noraStatus = when (uiState.engineState) {
        is EngineLoadState.Loaded -> NoraStatus.READY
        is EngineLoadState.Loading -> NoraStatus.THINKING
        is EngineLoadState.Error -> NoraStatus.ERROR
        EngineLoadState.Unloaded -> NoraStatus.OFFLINE
    }

    Scaffold(
        containerColor = NoraColors.Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── 顶部状态栏（离线指示 + Nora 状态 + 呼吸灯） ──
            SanctuaryStatusBar(noraStatus = noraStatus)

            Spacer(Modifier.weight(1f))

            // ── 中央：Nora 呼吸光环（Canvas 版） ──
            val orbColor = when (noraStatus) {
                NoraStatus.READY -> NoraColors.NoraOrange
                NoraStatus.THINKING -> NoraColors.NoraThinking
                NoraStatus.ERROR -> NoraColors.NoraError
                NoraStatus.OFFLINE -> NoraColors.NoraOffline
            }
            NoraBreathingOrb(
                size = 160.dp,
                color = orbColor
            )

            Spacer(Modifier.height(16.dp))

            // Nora 状态文案
            val statusText = when (noraStatus) {
                NoraStatus.READY -> "Nora 在线"
                NoraStatus.THINKING -> "Nora 正在苏醒..."
                NoraStatus.ERROR -> "Nora 遇到了问题"
                NoraStatus.OFFLINE -> "Nora 离线"
            }
            Text(
                text = statusText,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = NoraColors.PrimaryText
            )

            Spacer(Modifier.height(6.dp))

            // 呼吸光点 + 状态指示器
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                BreathingDot(
                    size = 8.dp,
                    color = when (noraStatus) {
                        NoraStatus.READY -> NoraColors.NoraReady
                        NoraStatus.THINKING -> NoraColors.NoraThinking
                        NoraStatus.ERROR -> NoraColors.NoraError
                        NoraStatus.OFFLINE -> NoraColors.NoraOffline
                    }
                )
                Spacer(Modifier.width(8.dp))
                NoraStatusIndicator(status = noraStatus)
            }

            Spacer(Modifier.weight(1f))

            // ── 底部三按钮导航 ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SanctuaryNavButton(
                    label = "对话",
                    onClick = onNavigateToChat
                )
                SanctuaryNavButton(
                    label = "日志",
                    onClick = onNavigateToLog
                )
                SanctuaryNavButton(
                    label = "技能",
                    onClick = onNavigateToSkill
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// 顶部状态栏 — 宪法 3.4.B
// 断开云图标 + "安全模式 | 所有数据在本地"
// ═══════════════════════════════════════════════════════

@Composable
private fun SanctuaryStatusBar(noraStatus: NoraStatus) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp),
        shape = NoraShapes.TagShape,
        color = NoraColors.Surface.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // 断开云图标（用 CircleShape + 穿透线模拟）
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(NoraColors.NoraReady.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(NoraColors.NoraReady)
                )
            }

            Spacer(Modifier.width(8.dp))

            Text(
                text = "安全模式 ｜ 所有数据在本地",
                fontSize = 12.sp,
                color = NoraColors.SecondaryText,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ═══════════════════════════════════════════════════════
// 底部导航按钮 — Apple 简洁策略
// ═══════════════════════════════════════════════════════

@Composable
private fun SanctuaryNavButton(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clickable { onClick() },
        shape = NoraShapes.ButtonShape,
        color = NoraColors.SurfaceElevated
    ) {
        Column(
            modifier = Modifier
                .width(96.dp)
                .padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = NoraColors.PrimaryText
            )
        }
    }
}
