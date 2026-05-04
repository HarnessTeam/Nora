package ai.nora.ui.skill

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.nora.theme.NoraColors
import ai.nora.theme.NoraShapes

// ═══════════════════════════════════════════════════════════════
// SkillTreeScreen — 技能树页（宪法 3.4.C + Phase 5 技能树）
// 底部三按钮 → 技能：Nora 能力可视化
// 技能三态：已点亮（呼吸动画）/ 训练中（闪烁）/ 未点亮（灰暗）
// ═══════════════════════════════════════════════════════════════

private data class SkillNode(
    val icon: String,
    val name: String,
    val description: String,
    val state: SkillState
)

private enum class SkillState { LIT, TRAINING, DARK }

@Composable
fun SkillTreeScreen(
    modifier: Modifier = Modifier
) {
    // 技能节点数据（Phase 5 完整实现）
    val skills = listOf(
        SkillNode("💬", "对话", "Nora 的核心能力，随时待命", SkillState.LIT),
        SkillNode("🔔", "通知感知", "理解手机通知，自动摘要", SkillState.LIT),
        SkillNode("📄", "文件理解", "读取授权文件，融入上下文", SkillState.TRAINING),
        SkillNode("🧠", "记忆管理", "长期记忆，个性化交互", SkillState.TRAINING),
        SkillNode("🛡️", "隐私守护", "数据主权，完全本地", SkillState.LIT),
        SkillNode("⚡", "技能觉醒", "让 Nora 学这个", SkillState.DARK),
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
                text = "Nora 的技能",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = NoraColors.PrimaryText
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "每一次交互，都是成长的印记",
                style = MaterialTheme.typography.bodyMedium,
                color = NoraColors.SecondaryText
            )

            Spacer(Modifier.height(24.dp))

            // ── 技能统计卡片 ──
            val litCount = skills.count { it.state == SkillState.LIT }
            val trainingCount = skills.count { it.state == SkillState.TRAINING }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = NoraShapes.QuickActionCardShape,
                color = NoraColors.SurfaceElevated
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SkillStatItem(count = litCount, label = "已点亮", color = NoraColors.NoraOrange)
                    SkillStatItem(count = trainingCount, label = "训练中", color = NoraColors.NoraThinking)
                    SkillStatItem(count = skills.size - litCount - trainingCount, label = "未解锁", color = NoraColors.NoraOffline)
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── 技能节点列表 ──
            skills.forEach { skill ->
                SkillCard(skill = skill)
                Spacer(Modifier.height(12.dp))
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SkillStatItem(count: Int, label: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = NoraColors.SecondaryText
        )
    }
}

@Composable
private fun SkillCard(skill: SkillNode) {
    val (bgColor, textColor, dotColor, indicatorText) = when (skill.state) {
        SkillState.LIT -> listOf(
            NoraColors.Surface,
            NoraColors.PrimaryText,
            NoraColors.NoraOrange,
            "已点亮"
        )
        SkillState.TRAINING -> listOf(
            NoraColors.Surface.copy(alpha = 0.7f),
            NoraColors.SecondaryText,
            NoraColors.NoraThinking,
            "训练中"
        )
        SkillState.DARK -> listOf(
            NoraColors.Surface.copy(alpha = 0.4f),
            NoraColors.TertiaryText,
            NoraColors.NoraOffline,
            "未解锁"
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "skillPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (skill.state == SkillState.TRAINING) 0.4f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (skill.state == SkillState.LIT) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = bgColor as androidx.compose.ui.graphics.Color
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 技能图标 + 呼吸动画
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .scale(if (skill.state == SkillState.LIT) pulseScale else 1f)
                    .alpha(pulseAlpha)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when (skill.state) {
                            SkillState.LIT -> NoraColors.NoraOrange.copy(alpha = 0.15f)
                            SkillState.TRAINING -> NoraColors.NoraThinking.copy(alpha = 0.1f)
                            SkillState.DARK -> NoraColors.Surface.copy(alpha = 0.5f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(skill.icon, fontSize = 22.sp)
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = skill.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor as androidx.compose.ui.graphics.Color
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = skill.description,
                    fontSize = 12.sp,
                    color = NoraColors.SecondaryText
                )
            }

            // 状态指示
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(dotColor as androidx.compose.ui.graphics.Color)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = indicatorText as String,
                    fontSize = 11.sp,
                    color = NoraColors.SecondaryText
                )
            }
        }
    }
}
