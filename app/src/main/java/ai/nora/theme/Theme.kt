package ai.nora.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Nora 暗色 ColorScheme — 宪法强制，不跟随系统
// primary = NoraOrange (#FF6B6B) 是唯一暖色
private val NoraDarkColorScheme = darkColorScheme(
    primary = NoraColors.NoraOrange,       // #FF6B6B — 唯一暖色
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = NoraColors.Surface, // #1E1E1E
    onPrimaryContainer = NoraColors.TextPrimary, // #E0E0E0

    secondary = NoraColors.SurfaceEdge,    // #2C2C2C
    onSecondary = NoraColors.TextPrimary, // #E0E0E0
    secondaryContainer = NoraColors.Surface,
    onSecondaryContainer = NoraColors.TextSecondary,

    tertiary = NoraColors.MatrixGreen,     // 可选黑客模式
    onTertiary = Color(0xFF000000),

    background = NoraColors.Background,    // #121212
    onBackground = NoraColors.TextPrimary,  // #E0E0E0

    surface = NoraColors.Surface,           // #1E1E1E
    onSurface = NoraColors.TextPrimary,
    surfaceVariant = NoraColors.SurfaceEdge,// #2C2C2C
    onSurfaceVariant = NoraColors.TextSecondary,

    outline = NoraColors.SurfaceEdge,
)

@Composable
fun NoraTheme(
    content: @Composable () -> Unit
) {
    // 强制暗色，永不跟随系统主题
    MaterialTheme(
        colorScheme = NoraDarkColorScheme,
        typography = NoraTypography,
        content = content
    )
}
