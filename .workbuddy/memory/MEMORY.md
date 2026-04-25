# MEMORY.md — 长期记忆

## Nora 设计系统（DesignKit）
- **设计系统文件**：`C:\Users\28767\WorkBuddy\local-agent\.workbuddy\memory\nora-design-system.md`
- **核心组件路径**：`ai/nora/ui/design/NoraIcons.kt`（NoraLogo/BreathingDot/NoraStatusIndicator）
- **色彩系统**：`ai/nora/theme/NoraColors.kt`（PrimaryText/SecondaryText/Divider 是新属性名）
- **圆角规范**：`ai/nora/theme/NoraShapes.kt`（InputBar=24dp, MessageBubble=16dp, Tag=8dp）
- **Bug 记录**：ChatScreen.kt 有重复 NoraColors object（已修复），Theme.kt 引用旧颜色名（已同步），OutlinedTextField trailing comma 漏掉（已修复）
- **编译注意**：Material3 1.7+ (BOM 2026) Typography 不支持 displayLarge/displayMedium/displaySmall 参数
- **Phase Design 进度**：✅ 8/8 Steps 完成（100%）— Gate Passed（2026-04-25 16:01）

## Nora Android 项目（C:\Users\28767\WorkBuddy\local-agent）

### 项目概述
- **产品**: Nora — 离线 Android AI 智能体（数字生命）
- **包名**: `ai.nora`
- **Phase 状态**: Phase 1 🔄 IN PROGRESS（设计系统已就绪，Step 7 测试待推进）
- **技术栈**: Kotlin + Jetpack Compose + Room + ExecuTorch + Navigation3

### 当前 Phase 1 进度
- ✅ Step 1: Application 级 Room 初始化（NoraApp.kt）— git 9a363b5
- ✅ Step 2: Navigation.kt 注入 DataRepository — git ac98425
- ✅ Step 3: ChatViewModel 构造函数注入 DataRepository — git 216ff4b
- ✅ Step 4: write-ahead 持久化（sendMessage/sendMessageStream）— git 4183731
  - `ensureConversation()` / `persistUserMessage()` / `persistAssistantMessage()`
  - DataRepository 新增 `updateConversationTimestamp()`
- ✅ Step 5: 加载对话时从 Room 恢复历史消息 — git 22670e1
  - ConversationDao: `getAllConversationsOnce()` suspend 查询
  - DataRepository: `getMostRecentConversationId()` 返回最近对话 ID
  - ChatViewModel: `init` 块 + `loadConversation()` 恢复历史
- ✅ Step 6: 新建/切换对话功能 — git 26f494e
  - DataRepository: `deleteConversation()` / `getConversationTitle()`
  - ChatViewModel: `createNewConversation()` / `switchConversation()` / `deleteConversation()`
  - ChatUiState: `conversations` + `currentConversationTitle`
  - ChatScreen: TopAppBar 可点击 → ModalBottomSheet 对话列表
  - 踩坑: Column 内 forEach 替代 items()；`by mutableStateOf` 需 setValue
- **NEXT_STEP**: Phase 1 Step 7 — Phase 1 Instrument 测试
- **Phase 1 进度**: 6/7 Steps（85.7%）

### Phase 0 归档
- ✅ 全部 18 Steps 完成
- ✅ Phase 0 Gate: Instrument 10/10 passed, 宪法合规审计 5/5 passed
- ✅ git commit d92fb27

### Phase 6 概要（感知层）
- 目标：Nora 实现「感知 (Sense)」宪法维度 — 通知监听 + 自动摘要 + 文件上下文
- 核心技术：NotificationListenerService + WorkManager PeriodicWork + SAF + Qwen3 LLM
- 依赖：Phase 1 (Room) → Phase 2 (Navigation) → Phase 6
- 关键约束：INTERNET 禁令（宪法红线）、Android 15 OTP 过滤（接受）、Room ≤1000 条
- 调研报告：`.workbuddy/memory/nora-notification-deep-research.md`

### 踩坑记录
- FontFamily("String") 在某些 Compose 版本报错 "expected Boolean"：改用 FontFamily.Default / FontFamily.Monospace 替代
- build.bat 只执行 assembleDebug，不传参数；Instrument 测试需直接调用 gradlew.bat 并在 cmd /c 中 set 环境变量
- MessageDao: 不存在独立文件，实为 `ConversationDao.kt` 同包内嵌的 DAO 接口
- NoraIcons.kt: ImageVector 属性（val）不能当 Composable 函数调用，需用 `Icon(imageVector = NoraXxx, ...)` 包装
- Navigation.kt: `collectAsStateWithLifecycle(initialValue=false)` 导致首次渲染默认 Setup；改用 `initialValue=null as Boolean?` + `if(modelLoaded==false) Setup else Chat`
- PreferencesManager.setModelLoaded() 之前从未被调用，导致 DataStore modelLoaded 永远为 false；已在 SetupViewModel EngineLoadState.Loaded 时调用

### 环境
- JDK: `C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot`
- Gradle opts: `-Xmx1536m`
- ADB: `C:\Users\28767\AppData\Local\Android\Sdk\platform-tools\adb.exe`（不在 PATH，需用完整路径）
- 模拟器: medium_phone（API 36, x86_64）
- 编译方案: `build.bat`（仅 assembleDebug）；Instrument 测试需直接调用 `cmd /c "set JAVA_HOME=...&& set ANDROID_HOME=...&& gradlew.bat connectedDebugAndroidTest"`

### GitHub 部署
- 仓库: https://github.com/HarnessTeam/Nora
- 已清理 model.pte (469MB) 并从历史中移除
- README.md 包含模型下载指南和 AI 助手下载提示
- 截图: assets/screenshot.png（用户需自行提供）

### 踩坑记录（补充）
- Type.kt / Color.kt: 已在之前 Step 中删除，无需重复操作
- MainScreenViewModelTest.kt: 已删除，无需操作
- 应用图标: 已替换为自定义 Nora 图标，删除了 adaptive icon（mipmap-anydpi-v26）
- GitHub 大文件: model.pte 超过 100MB，使用 `git filter-branch` 从历史移除
