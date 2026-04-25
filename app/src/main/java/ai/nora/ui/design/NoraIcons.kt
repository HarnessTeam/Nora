package ai.nora.ui.design

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.nora.theme.NoraColors

// ═══════════════════════════════════════════════════════
// NoraIcons — Apple HIG + 豆包/ChatGPT 品牌图标组件
// 基于 nora-design-system.md Section 5
// ═══════════════════════════════════════════════════════

// ═══════════════════════════════════════════════════════
// 1. Nora 品牌 Logo（大圆 + N 字母）
// ═══════════════════════════════════════════════════════

@Composable
fun NoraLogo(
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    showGlow: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(size)
            .then(
                if (onClick != null) Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ) else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        // 外圈光晕（可选，用于安全屋/欢迎态）
        if (showGlow) {
            val infiniteTransition = rememberInfiniteTransition(label = "logo_glow")
            val glowAlpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 0.7f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "glow_alpha"
            )
            Canvas(modifier = Modifier.size(size)) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            NoraColors.NoraOrange.copy(alpha = glowAlpha),
                            NoraColors.NoraOrange.copy(alpha = 0f)
                        ),
                        center = Offset(this.size.width / 2, this.size.height / 2),
                        radius = this.size.minDimension / 2
                    )
                )
            }
        }

        // 主体圆形
        val gradientColors = listOf(NoraColors.NoraOrange, NoraColors.NoraOrangeLight)

        Box(
            modifier = Modifier
                .size(size * 0.78f)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(colors = gradientColors)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "N",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.45f).sp
            )
        }
    }
}

// ═══════════════════════════════════════════════════════
// 2. 呼吸光点动画（Nora 头像呼吸效果）
// ═══════════════════════════════════════════════════════

@Composable
fun BreathingDot(
    modifier: Modifier = Modifier,
    size: Dp = 12.dp,
    color: Color = NoraColors.NoraOrange
) {
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing_scale"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .clip(CircleShape)
            .background(color)
    )
}

// ═══════════════════════════════════════════════════════
// 3. 状态指示器（就绪/思考中/异常）
// ═══════════════════════════════════════════════════════

enum class NoraStatus {
    READY,      // 就绪态 🟢
    THINKING,   // 思考中 🟡
    ERROR,      // 异常态 🔴
    OFFLINE     // 离线态 ⚪
}

@Composable
fun NoraStatusIndicator(
    status: NoraStatus,
    modifier: Modifier = Modifier
) {
    val (color, label) = when (status) {
        NoraStatus.READY    -> NoraColors.NoraReady    to "就绪"
        NoraStatus.THINKING -> NoraColors.NoraThinking to "思考中..."
        NoraStatus.ERROR    -> NoraColors.NoraError    to "异常"
        NoraStatus.OFFLINE  -> NoraColors.NoraOffline  to "离线"
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = color,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ═══════════════════════════════════════════════════════
// 4. 简洁状态 Dot（纯色点，用于 TopBar 右侧）
// ═══════════════════════════════════════════════════════

@Composable
fun NoraStatusDot(
    status: NoraStatus,
    modifier: Modifier = Modifier
) {
    val color = when (status) {
        NoraStatus.READY    -> NoraColors.NoraReady
        NoraStatus.THINKING -> NoraColors.NoraThinking
        NoraStatus.ERROR    -> NoraColors.NoraError
        NoraStatus.OFFLINE  -> NoraColors.NoraOffline
    }

    Box(
        modifier = modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(color)
    )
}
