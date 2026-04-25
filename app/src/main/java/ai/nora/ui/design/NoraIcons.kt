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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector
import ai.nora.theme.NoraColors
import ai.nora.theme.NoraShapes

// ═══════════════════════════════════════════════════════
// NoraIcons — ImageVector 版本（用于 Icon() 组件和 QuickAction）
// 使用 Material Icons Extended 扩展库，统一 NoraOrange 描边风格
// ═══════════════════════════════════════════════════════

/** 描述/读文件图标 */
val NoraDescription: ImageVector get() = Icons.Filled.Description

/** 通知图标 */
val NoraNotifications: ImageVector get() = Icons.Filled.Notifications

/** 代码图标 */
val NoraCode: ImageVector get() = Icons.AutoMirrored.Filled.MenuBook

/** 发送箭头图标 */
val NoraSendArrow: ImageVector get() = Icons.AutoMirrored.Filled.Send

/** 停止图标 */
val NoraStop: ImageVector get() = Icons.Filled.Stop

/** 添加图标 */
val NoraAdd: ImageVector get() = Icons.Filled.Add

/** 删除图标 */
val NoraDelete: ImageVector get() = Icons.Filled.Delete

/** 菜单图标 */
val NoraMenu: ImageVector get() = Icons.Filled.Description

/** 内存/模型图标 */
val NoraMemory: ImageVector get() = Icons.Filled.Folder

/** 刷新图标 */
val NoraRefresh: ImageVector get() = Icons.Filled.Refresh

/** 检查圆圈图标 */
val NoraCheckCircle: ImageVector get() = Icons.Filled.CheckCircle

/** 文件夹图标 */
val NoraFolder: ImageVector get() = Icons.Filled.Folder

/** 勾选图标 */
val NoraCheck: ImageVector get() = Icons.Filled.Check

/** 向上箭头图标 */
val NoraArrowUp: ImageVector get() = Icons.AutoMirrored.Filled.Send

// ═══════════════════════════════════════════════════════
// 1. NoraLogo（大圆 + N 字母）— Canvas Composable
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
        val gradientColors = listOf(NoraColors.NoraOrange, NoraColors.NoraOrangeLight)
        Box(
            modifier = Modifier
                .size(size * 0.78f)
                .clip(CircleShape)
                .background(Brush.linearGradient(colors = gradientColors)),
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
// 2. 呼吸光点
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
// 2b. 呼吸光环（Canvas 版 — 安全屋核心动效）
// 设计规范：1500ms LinearEasing 呼吸节奏，径向渐变光环
// 宪法 3.5: 心跳/呼吸节奏 — 待机呼吸感
// ═══════════════════════════════════════════════════════

@Composable
fun NoraBreathingOrb(
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
    color: Color = NoraColors.NoraOrange
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_breathing")

    // 呼吸缩放：0.9 → 1.1（微妙，不过度）
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb_scale"
    )

    // 光晕透明度：0.15 → 0.45（呼吸节奏）
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb_glow"
    )

    // 外环旋转（极慢，增加生命感）
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orb_rotation"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // 外层光晕 — Canvas 径向渐变
        Canvas(modifier = Modifier.size(size).scale(scale)) {
            val center = Offset(this.size.width / 2, this.size.height / 2)
            val radius = this.size.minDimension / 2

            // 最外层柔和光晕
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        color.copy(alpha = glowAlpha * 0.4f),
                        color.copy(alpha = glowAlpha * 0.15f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius
                )
            )

            // 中层光环
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        color.copy(alpha = glowAlpha * 0.6f),
                        color.copy(alpha = 0f)
                    ),
                    center = center,
                    radius = radius * 0.7f
                )
            )
        }

        // 核心圆 — NoraOrange 渐变
        Box(
            modifier = Modifier
                .size(size * 0.45f)
                .scale(scale)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(color, NoraColors.NoraOrangeLight)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // "N" 字母
            Text(
                text = "N",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.2f).sp
            )
        }
    }
}

// ═══════════════════════════════════════════════════════
// 3. 状态指示器
// ═══════════════════════════════════════════════════════

enum class NoraStatus {
    READY,
    THINKING,
    ERROR,
    OFFLINE
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
        shape = NoraShapes.TagShape,
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
