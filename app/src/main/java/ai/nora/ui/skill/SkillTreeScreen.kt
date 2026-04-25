package ai.nora.ui.skill

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ai.nora.theme.NoraColors

// ═══════════════════════════════════════════════════════
// SkillTreeScreen — 技能树页（占位，Phase 5 实现）
// 宪法 3.4.C: 底部三按钮 → 技能
// ═══════════════════════════════════════════════════════

@Composable
fun SkillTreeScreen(
    modifier: Modifier = Modifier
) {
    Scaffold(
        containerColor = NoraColors.Background
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Nora 的技能",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = NoraColors.PrimaryText
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "每一次交互，都是成长的印记",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NoraColors.SecondaryText
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    text = "功能开发中 · Phase 5",
                    style = MaterialTheme.typography.bodySmall,
                    color = NoraColors.TertiaryText
                )
            }
        }
    }
}
