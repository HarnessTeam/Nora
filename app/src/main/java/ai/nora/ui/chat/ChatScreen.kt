package ai.nora.ui.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector


import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import ai.nora.llm.EngineLoadState
import ai.nora.model.ChatMessage
import ai.nora.model.ConversationEntity
import ai.nora.theme.NoraColors
import ai.nora.theme.NoraShapes
import ai.nora.ui.design.NoraAdd
import ai.nora.ui.design.NoraCode
import ai.nora.ui.design.NoraDelete
import ai.nora.ui.design.NoraDescription
import ai.nora.ui.design.NoraLogo
import ai.nora.ui.design.NoraNotifications
import ai.nora.ui.design.NoraSendArrow
import ai.nora.ui.design.NoraStatus
import ai.nora.ui.design.NoraStatusIndicator
import ai.nora.ui.design.NoraStop

// ============================================================
// Nora App v2 - 核心设计原则
// 1. 零摩擦进入 - 开屏即对话
// 2. 渐进式引导 - 快捷功能卡片
// 3. 安静界面 - 技术细节隐藏
// 4. AI Native 视觉 - 科技感 + 温暖感
// ============================================================

// ============ 快捷功能卡片数据 ============
data class QuickAction(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val action: String // 内部标识
)

val quickActions = listOf(
    QuickAction(NoraDescription, "读文件", "加载本地文件", "read_file"),
    QuickAction(NoraNotifications, "看通知", "聚合通知摘要", "notifications"),
    QuickAction(NoraCode, "写代码", "代码生成解释", "code"),
)

// ============ TopBar - Apple HIG 标准（56dp，无设置入口） ============
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoraTopBar(
    onMenuClick: () -> Unit,
    noraStatus: NoraStatus = NoraStatus.READY,
    modelStatus: ModelStatus = ModelStatus.READY
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onMenuClick() }
            ) {
                // Nora 品牌 Logo — Apple 简洁策略
                NoraLogo(size = 32.dp)
                Spacer(Modifier.width(10.dp))
                Text(
                    "Nora",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = NoraColors.NoraOrange
                )
                Spacer(Modifier.width(8.dp))
                // Apple 风格状态指示器
                ModelStatusIndicator(status = modelStatus)
            }
        },
        // 🚫 禁止：设置入口 — Apple Clarity 原则
        actions = { },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

enum class ModelStatus { READY, LOADING, ERROR }

@Composable
private fun ModelStatusIndicator(status: ModelStatus) {
    val (color, text) = when (status) {
        ModelStatus.READY -> NoraColors.NoraReady to "就绪"
        ModelStatus.LOADING -> NoraColors.NoraThinking to "加载中"
        ModelStatus.ERROR -> NoraColors.NoraError to "异常"
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(NoraShapes.TagShape)
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text,
            fontSize = 11.sp,
            color = color
        )
    }
}

// ============ 欢迎区块 - 空状态引导 ============
@Composable
fun WelcomeSection(
    isFirstTime: Boolean = false,
    onQuickAction: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 发光 Logo
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            NoraColors.NoraOrange,
                            NoraColors.NoraOrange.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(NoraColors.NoraOrange),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "N",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        Text(
            if (isFirstTime) "欢迎使用 Nora" else "欢迎回来",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "你的本地 AI 助手 · 数据永不离开设备",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(32.dp))

        // 快捷功能卡片
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(quickActions) { action ->
                QuickActionCard(action = action, onClick = { onQuickAction(action.action) })
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    action: QuickAction,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(120.dp)
            .clickable { onClick() },
        shape = NoraShapes.QuickActionCardShape,
        color = NoraColors.SurfaceElevated,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                action.icon,
                contentDescription = null,
                tint = NoraColors.NoraOrange,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.height(10.dp))
            Text(
                action.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                action.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ============ 消息气泡 - Apple HIG + Nora 宪法 ============
// 用户：NoraOrange 背景 + 白字
// 助手：Surface 背景 + Divider 描边 + PrimaryText
@Composable
fun MessageBubble(message: ChatMessage) {
    val isUser = message.role == "user"
    val isError = message.role == "system"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = NoraShapes.MessageBubbleShape,
            color = when {
                isError -> MaterialTheme.colorScheme.errorContainer
                isUser -> NoraColors.NoraOrange  // 用户消息：NoraOrange
                else -> NoraColors.AssistantBubbleBg  // 助手消息：Surface (#1E1E1E)
            },
            border = if (!isUser && !isError) {
                androidx.compose.foundation.BorderStroke(1.dp, NoraColors.AssistantBubbleBorder)
            } else null,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            val textColor = when {
                isError -> MaterialTheme.colorScheme.onErrorContainer
                isUser -> NoraColors.UserBubbleText  // 白字
                else -> NoraColors.AssistantBubbleText  // PrimaryText
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = message.content.ifEmpty {
                        if (message.isStreaming) " " else ""
                    },
                    color = textColor,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    fontFamily = FontFamily.SansSerif,
                    modifier = Modifier.weight(1f, fill = false)
                )

                // 流式输出光标
                if (message.isStreaming) {
                    PulsingCursor()
                }
            }
        }
    }
}

@Composable
private fun PulsingCursor() {
    val infiniteTransition = rememberInfiniteTransition(label = "cursor")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor_alpha"
    )

    Text(
        "▊",
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
        fontSize = 15.sp,
        modifier = Modifier.padding(start = 2.dp)
    )
}

// ============ 输入框 - 大圆角胶囊设计 ============
@Composable
fun NoraInputBar(
    inputText: String,
    isGenerating: Boolean,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onStop: () -> Unit
) {
    Surface(
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.background
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        "发送消息给 Nora...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                maxLines = 4,
                shape = NoraShapes.InputBarShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NoraColors.NoraOrange,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                trailingIcon = {
                    if (isGenerating) {
                            IconButton(onClick = onStop) {
                            Icon(
                            imageVector = NoraStop,
                            contentDescription = "停止",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        }
                    }
                }
            )

            Spacer(Modifier.width(10.dp))

            // 发送按钮 - ArrowUp 设计
            Surface(
                modifier = Modifier
                    .size(48.dp)
                    .clickable(enabled = inputText.isNotBlank() && !isGenerating) { onSend() },
                shape = CircleShape,
                color = if (inputText.isNotBlank() && !isGenerating)
                    NoraColors.NoraOrange
                else
                    MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = NoraSendArrow,
                            contentDescription = "发送",
                            modifier = Modifier.size(24.dp),
                            tint = if (inputText.isNotBlank())
                                Color.White
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// ============ 对话列表底部表单 ============
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationListSheet(
    conversations: List<ConversationEntity>,
    currentConversationId: Long?,
    onNewConversation: () -> Unit,
    onSelectConversation: (Long) -> Unit,
    onDeleteConversation: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // 新建对话按钮
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNewConversation() }
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = NoraAdd,
                    contentDescription = "新建对话",
                    modifier = Modifier.size(24.dp),
                    tint = NoraColors.NoraOrange
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "新建对话",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = NoraColors.NoraOrange
                )
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp))

            // 对话列表
            if (conversations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "暂无对话记录",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                conversations.forEach { conv ->
                    val isActive = conv.id == currentConversationId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectConversation(conv.id) }
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 活跃指示器
                        if (isActive) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(NoraColors.MatrixGreen)
                            )
                            Spacer(Modifier.width(12.dp))
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                conv.title,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (isActive)
                                    NoraColors.NoraOrange
                                else
                                    MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                formatTimestamp(conv.updatedAt),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(onClick = { onDeleteConversation(conv.id) }) {
                        Icon(
                            imageVector = NoraDelete,
                            contentDescription = "删除对话",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                        )
                        }
                    }
                }
            }
        }
    }
}

// ============ 时间格式化 ============
@Composable
private fun formatTimestamp(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60_000 -> "刚刚"
        diff < 3_600_000 -> "${diff / 60_000} 分钟前"
        diff < 86_400_000 -> "${diff / 3_600_000} 小时前"
        else -> "${diff / 86_400_000} 天前"
    }
}

// ============ 主 ChatScreen 整合（使用 theme.NoraColors） ============
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onUnloadModel: () -> Unit = {},
    viewModel: ChatViewModel,
    modelStatus: ModelStatus = ModelStatus.READY  // backward compat, now derived from uiState
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    var showConversationSheet by remember { mutableStateOf(false) }

    // 从 engineState 推导 ModelStatus（覆盖外部传入的默认值）
    val derivedModelStatus = when (uiState.engineState) {
        is EngineLoadState.Loaded -> ModelStatus.READY
        is EngineLoadState.Loading -> ModelStatus.LOADING
        is EngineLoadState.Error -> ModelStatus.ERROR
        EngineLoadState.Unloaded -> ModelStatus.LOADING  // 未加载时视为加载中
    }

    // 自动滚动到底部
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    val lastMsg = uiState.messages.lastOrNull()
    LaunchedEffect(lastMsg?.content) {
        if (lastMsg?.isStreaming == true && uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            NoraTopBar(
                onMenuClick = { showConversationSheet = true },
                modelStatus = derivedModelStatus
            )
        },
        bottomBar = {
            NoraInputBar(
                inputText = uiState.inputText,
                isGenerating = uiState.isGenerating,
                onInputChange = { viewModel.updateInput(it) },
                onSend = { viewModel.sendMessageStream(uiState.inputText) },
                onStop = { viewModel.stopGeneration() }
            )
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // 欢迎区块（空状态）
            if (uiState.messages.isEmpty()) {
                item {
                    WelcomeSection(
                        isFirstTime = uiState.conversations.isEmpty(),
                        onQuickAction = { action ->
                            // 快捷操作处理
                            when (action) {
                                "read_file" -> { /* TODO: 文件选择 */ }
                                "notifications" -> { /* TODO: 通知聚合 */ }
                                "code" -> { viewModel.updateInput("帮我写一段代码") }
                            }
                        }
                    )
                }
            }

            // 消息列表
            items(uiState.messages, key = { "${it.role}-${it.timestamp}" }) { message ->
                MessageBubble(message = message)
            }

            // 底部安全间距
            item { Spacer(Modifier.height(8.dp)) }
        }
    }

    // 对话列表底部表单
    if (showConversationSheet) {
        ConversationListSheet(
            conversations = uiState.conversations,
            currentConversationId = uiState.currentConversationId,
            onNewConversation = {
                viewModel.createNewConversation()
                showConversationSheet = false
            },
            onSelectConversation = { id ->
                viewModel.switchConversation(id)
                showConversationSheet = false
            },
            onDeleteConversation = { id ->
                viewModel.deleteConversation(id)
            },
            onDismiss = { showConversationSheet = false }
        )
    }

    // 模型加载中 / 错误覆盖层
    if (!uiState.isModelReady) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (uiState.modelError != null) {
                    // 错误状态
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(NoraColors.NoraError.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "!",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = NoraColors.NoraError
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "模型加载失败",
                        style = MaterialTheme.typography.titleMedium,
                        color = NoraColors.NoraError
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        uiState.modelError ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    // 加载中状态
                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp),
                        color = NoraColors.NoraOrange,
                        strokeWidth = 3.dp
                    )
                    Spacer(Modifier.height(20.dp))
                    val loadingMsg = when (val s = uiState.engineState) {
                        is EngineLoadState.Loading -> s.message.ifEmpty { "正在加载模型..." }
                        else -> "正在准备 Nora..."
                    }
                    Text(
                        loadingMsg,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
