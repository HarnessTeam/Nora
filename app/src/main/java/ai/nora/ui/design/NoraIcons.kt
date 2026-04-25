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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathData
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.sp
import ai.nora.theme.NoraColors
import ai.nora.theme.NoraShapes

// ═══════════════════════════════════════════════════════
// NoraIcons — Apple HIG + Nora 品牌图标组件
// 使用 Compose ImageVector.Builder + PathData.fromString()
// ═══════════════════════════════════════════════════════

// SVG PathData 解析辅助
private fun svgPath(d: String): PathData = PathData.fromString(d)

// ═══════════════════════════════════════════════════════
// 1. NoraLogo（大圆 + N 字母）
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

// ═══════════════════════════════════════════════════════
// 4. 导航图标（viewportSize=24x24, NoraOrange 描边）
// ═══════════════════════════════════════════════════════

// Send Arrow — 发送按钮
val NoraSendArrow: ImageVector
    get() = ImageVector.Builder(
        name = "NoraSendArrow",
        viewportWidth = 24f.toDp(),
        viewportHeight = 24f.toDp()
    ).apply {
        addPath(
            pathData = svgPath("M12 4l-1.41 1.41L16.17 11H4v2h12.17l-5.58 5.59L12 20l8-8z"),
            fill = null,
            stroke = SolidColor(NoraColors.NoraOrange),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        )
    }.build()

// Stop — 停止（红色方块）
val NoraStop: ImageVector
    get() = ImageVector.Builder(
        name = "NoraStop",
        viewportWidth = 24f.dp,
        viewportHeight = 24f.dp
    ).apply {
        addPath(
            pathData = svgPath("M6 6h12v12H6z"),
            fill = SolidColor(NoraColors.NoraError),
            stroke = null
        )
    }.build()

// Add — 加号
val NoraAdd: ImageVector
    get() = ImageVector.Builder(
        name = "NoraAdd",
        viewportWidth = 24f.dp,
        viewportHeight = 24f.dp
    ).apply {
        addPath(
            pathData = svgPath("M12 5v14M5 12h14"),
            fill = null,
            stroke = SolidColor(NoraColors.NoraOrange),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        )
    }.build()

// Delete — 垃圾桶
val NoraDelete: ImageVector
    get() = ImageVector.Builder(
        name = "NoraDelete",
        viewportWidth = 24f.dp,
        viewportHeight = 24f.dp
    ).apply {
        addPath(
            pathData = svgPath(
                "M6 7v13h12V7 M9 7V5h6v2 M4 7h16l-1.5 15h-13z"
            ),
            fill = null,
            stroke = SolidColor(NoraColors.NoraError),
            strokeLineWidth = 1.5f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        )
    }.build()

// Menu — 三横线
val NoraMenu: ImageVector
    get() = ImageVector.Builder(
        name = "NoraMenu",
        viewportWidth = 24f.dp,
        viewportHeight = 24f.dp
    ).apply {
        addPath(
            pathData = svgPath("M3 6h18M3 12h18M3 18h18"),
            fill = null,
            stroke = SolidColor(NoraColors.NoraOrange),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        )
    }.build()

// ═══════════════════════════════════════════════════════
// 5. 快捷功能图标
// ═══════════════════════════════════════════════════════

// Description — 文档
val NoraDescription: ImageVector
    get() = ImageVector.Builder(
        name = "NoraDescription",
        viewportWidth = 24f.dp,
        viewportHeight = 24f.dp
    ).apply {
        addPath(
            pathData = svgPath("M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"),
            fill = null,
            stroke = SolidColor(NoraColors.NoraOrange),
            strokeLineWidth = 1.5f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        )
        addPath(
            pathData = svgPath("M14 2v6h6M8 13h8M8 17h5"),
            fill = null,
            stroke = SolidColor(NoraColors.NoraOrange),
            strokeLineWidth = 1.5f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        )
    }.build()

// Notifications — 通知铃
val NoraNotifications: ImageVector
    get() = ImageVector.Builder(
        name = "NoraNotifications",
        viewportWidth = 24f.dp,
        viewportHeight = 24f.dp
    ).apply {
        addPath(
            pathData = svgPath("M18 16v-5c0-2-1-4-3-4S12 9 12 9V5M6 16v-5c0-2 1-4 3-4s3 2 3 4v5"),
            fill = null,
            stroke = SolidColor(NoraColors.NoraOrange),
            strokeLineWidth = 1.5f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        )
        addPath(
            pathData = svgPath("M19 20a2 2 0 01-2 2H7a2 2 0 01-2-2M12 20a2 2 0 002-2 2 2 0 00-2-2z"),
            fill = null,
            stroke = SolidColor(NoraColors.NoraOrange),
            strokeLineWidth = 1.5f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        )
    }.build()

// Code — 代码符号
val NoraCode: ImageVector
    get() = ImageVector.Builder("NoraCode", 24f, 24f).apply {
        addPath(
            pathData = svgPath("M16 18l6-6-6-6M8 6l-6 6 6 6"),
            fill = null,
            stroke = SolidColor(NoraColors.NoraOrange),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        )
    }.build()

// ═══════════════════════════════════════════════════════
// 6. Setup 页面图标
// ═══════════════════════════════════════════════════════

// Memory — 内存芯片
val NoraMemory: ImageVector
    get() = ImageVector.Builder("NoraMemory", 24f, 24f).apply {
        addPath(
            pathData = svgPath("M6 4h12v16H6zM10 4v16M14 4v16M18 4v16"),
            fill = null,
            stroke = SolidColor(NoraColors.NoraOrange),
            strokeLineWidth = 1.5f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        )
    }.build()

// Refresh — 刷新
val NoraRefresh: ImageVector
    get() = ImageVector.Builder("NoraRefresh", 24f, 24f).apply {
        addPath(
            pathData = svgPath("M1 4v6h6M23 20v-6h-6"),
            fill = null,
            stroke = SolidColor(NoraColors.NoraOrange),
            strokeLineWidth = 1.5f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        )
        addPath(
            pathData = svgPath(
                "M20.49 9A9 9 0 005.64 5.64L1 10m22 4l-4.64 4.36A9 9 0 013.51 15"
            ),
            fill = null,
            stroke = SolidColor(NoraColors.NoraOrange),
            strokeLineWidth = 1.5f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        )
    }.build()

// CheckCircle — 选中圆圈
val NoraCheckCircle: ImageVector
    get() = ImageVector.Builder("NoraCheckCircle", 24f, 24f).apply {
        addPath(
            pathData = svgPath("M22 11.08V12a10 10 0 11-5.93-9.14"),
            fill = null,
            stroke = SolidColor(NoraColors.NoraOrange),
            strokeLineWidth = 1.5f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        )
        addPath(
            pathData = svgPath("M22 4L12 14.01l-3-3"),
            fill = null,
            stroke = SolidColor(NoraColors.NoraOrange),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        )
    }.build()

// Folder — 文件夹
val NoraFolder: ImageVector
    get() = ImageVector.Builder("NoraFolder", 24f, 24f).apply {
        addPath(
            pathData = svgPath(
                "M22 19a2 2 0 01-2 2H4a2 2 0 01-2-2V5a2 2 0 012-2h5l2 3h9a2 2 0 012 2z"
            ),
            fill = null,
            stroke = SolidColor(NoraColors.NoraOrange),
            strokeLineWidth = 1.5f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        )
    }.build()

// Check — 对勾
val NoraCheck: ImageVector
    get() = ImageVector.Builder("NoraCheck", 24f, 24f).apply {
        addPath(
            pathData = svgPath("M20 6L9 17l-5-5"),
            fill = null,
            stroke = SolidColor(NoraColors.NoraOrange),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        )
    }.build()

// ArrowUp — 向上箭头
val NoraArrowUp: ImageVector
    get() = ImageVector.Builder("NoraArrowUp", 24f, 24f).apply {
        addPath(
            pathData = svgPath("M12 4l-8 8h5v8h6v-8h5z"),
            fill = null,
            stroke = SolidColor(NoraColors.NoraOrange),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        )
    }.build()
