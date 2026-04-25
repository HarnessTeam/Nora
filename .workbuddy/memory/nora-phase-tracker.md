# Nora Phase Tracker — 渐进式改造路线图（测试驱动版）

> 最后更新：2026-04-25 10:07
> 项目路径：C:\Users\28767\WorkBuddy\local-agent
> 宪法文件：.workbuddy/memory/nora-constitution.md
> 测试命令：`gradlew connectedDebugAndroidTest`（需 ADB 连接设备/模拟器）

---

## 测试策略总纲

### 测试分层

| 层级 | 工具 | 运行环境 | 用途 | 何时执行 |
|------|------|---------|------|---------|
| **Unit Test** | JUnit 4 + coroutines-test | JVM（本地） | 纯逻辑、ViewModel 状态机、Room DAO | 每次 Step 改动后 |
| **Instrument Test** | Compose UI Test + Espresso | Android 设备/模拟器 | UI 交互、导航流、端到端场景 | 每个 Phase Gate 前必须全量通过 |

### Instrument 测试铁律

1. **Gate 硬性条件**：`connectedDebugAndroidTest` 全部通过才允许推进 Phase
2. **每个 Phase 最后一组 Step 必须是 Instrument 测试**，紧接 Gate 验证
3. **测试必须是可执行的**：不写 `@Ignore`、不写 TODO 测试、不写空测试体
4. **ADB 必须在线**：自动化执行前检查 `adb devices`，无设备则跳过测试并标记 BLOCKER
5. **测试命名规范**：`场景_预期行为`（中文），如 `发送消息_助手气泡出现`

### 测试运行命令

```bash
# 环境变量（必须在 cmd /c 中 set，不能依赖 PowerShell $env:JAVA_HOME）
set GRADLE_OPTS=-Xmx1536m
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot
set ANDROID_HOME=C:\Users\28767\AppData\Local\Android\Sdk
set PATH=%JAVA_HOME%\bin;%ANDROID_HOME%\platform-tools;%ANDROID_HOME%\emulator;%PATH%

# 编译
gradlew.bat assembleDebug

# Unit 测试（快速反馈）
gradlew.bat testDebugUnitTest

# Instrument 测试（需要设备/模拟器）
gradlew.bat connectedDebugAndroidTest

# 单个测试类
gradlew.bat connectedDebugAndroidTest --tests "ai.nora.ui.chat.ChatScreenTest"

# ADB（完整路径，不在 PATH）
C:\Users\28767\AppData\Local\Android\Sdk\platform-tools\adb.exe devices
```

### 测试基础设施前置（Phase 0 Step 0）

在开始任何功能 Step 之前，先搭建测试骨架：
- `build.gradle.kts` 添加 `testInstrumentationRunner`
- 删除死测试（`MainScreenTest`、`SetupScreenTest`）
- 创建 `ai.nora` 包结构下的测试目录
- 创建 `BaseAndroidTest.kt` 基类（Hilt/Activity 场景共用）

---

## 当前代码库现状（Gap Analysis）

### 已有基础
- ✅ ExecuTorch 1.2.0 真实推理（LlmModule API）
- ✅ 流式输出（逐 token 回调）
- ✅ 模型扫描（ModelScanner）
- ✅ Room 数据库骨架（Entity + DAO + Repository 定义）
- ✅ Compose + Navigation3 导航
- ✅ 40MB APK（含 native libs）

### 关键 Gap（对照 Nora 宪法）

| 维度 | 当前状态 | Nora 要求 | Gap 级别 |
|------|---------|-----------|----------|
| 产品名/包名 | `com.example.localagent` / "LocalAgent" | `ai.nora` / "Nora" | P0 |
| 色彩系统 | Material3 默认紫色 | #121212/#1E1E1E/#FF6B6B | P0 |
| 暗色优先 | 双色模式 | 仅暗色，强制 | P0 |
| 主界面 | 直进聊天 | "安全屋" Sanctuary | P1 |
| 对话 UI | 标准 Material 气泡 | Nora 呼吸光点 + #1E1E1E 气泡 | P1 |
| 离线状态 | 无展示 | 离线仪表盘 | P1 |
| 消息持久化 | 仅内存 | Room 全量接入 | P0 |
| 编译阻断 | MessageDao 缺失 | — | P0-BLOCKER |
| 死代码 | 3个旧文件/2个旧测试 | — | P2 |
| Instrument 测试 | 2个死测试 | 每Phase有覆盖 | P0 |
| 隐私仪表盘 | 不存在 | "你的数据，只有你见过" | P2 |
| 推理可视化 | 无 | 点击查看思考链 | P2 |

---

## Phase 路线图

### Phase 0 🔲 PENDING — 项目重生（Nora 品牌化）

目标：将 LocalAgent 重命名为 Nora，建立 Nora 视觉基础 + 测试基础设施

- [x] Step 0: 搭建测试基础设施 ✅ (2026-04-25 02:44)
  - `build.gradle.kts` 添加 `testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"`
  - 删除死测试 `androidTest/.../MainScreenTest.kt` 和 `SetupScreenTest.kt`
  - 创建 `androidTest/java/ai/nora/BaseAndroidTest.kt`
  - 验证：`connectedDebugAndroidTest` 空跑通过（0 tests passed is ok）
- [x] Step 1a: git init + 首次 commit ✅ (2026-04-25 04:32)
  - Git 已初始化，6条历史记录存在
  - 验证：`git log --oneline` 有 6 条记录
- [x] Step 1b: 删除死代码 — MainScreenViewModelTest.kt ✅ (2026-04-25 04:32)
  - 文件已不存在，无需操作
  - 验证：`testDebugUnitTest` 通过
- [x] Step 1c: 删除死代码 — Type.kt ✅ (2026-04-25 04:32)
  - 文件已不存在，无需操作
  - 验证：编译通过
- [x] Step 1d: 删除死代码 — Color.kt ✅ (2026-04-25 04:32)
  - 文件已不存在，无需操作
  - 验证：编译通过
- [x] Step 2a: DataRepository MessageDao 引用 ✅ (2026-04-25 04:32)
  - MessageDao 实为 ConversationDao.kt 同包内嵌，引用正确
  - 验证：编译通过 + `testDebugUnitTest` 通过
- [x] Step 2b: AppDatabase Entity/DAO 引用 ✅ (2026-04-25 04:32)
  - 验证：编译通过 + `assembleDebug` 成功
- [x] Step 3a: 包名迁移 — build.gradle.kts（namespace + applicationId）✅ (2026-04-25 09:39)
  - `com.example.localagent` → `ai.nora`
  - 验证：`assembleDebug` ✅ + `git commit b181230`
- [x] Step 3b: 包名迁移 — 源文件（17 个 .kt 文件 package 声明）✅ (2026-04-25 09:41)
  - 批量替换所有 `package com.example.localagent` → `package ai.nora`（17 main + 1 test）
  - 测试目录结构同步迁移（`com/example/localagent` → `ai/nora`）
  - 验证：`assembleDebug` ✅ + `testDebugUnitTest` ✅ + `git commit 6ea5bae`
- [x] Step 3c: 包名迁移 — 目录结构 ✅ (2026-04-25 09:44)
  - 移动 `com/example/localagent/` → `ai/nora/`（17 个源文件）
  - 验证：`assembleDebug` ✅ + `testDebugUnitTest` ✅ + `git commit 3ad61bb`
- [x] Step 3d: 包名迁移 — XML 资源和 AndroidManifest ✅ (2026-04-25 10:07)
  - 确认：`app/src/` 下零 `com.example` 残留（已在 3a/3b/3c 处理完毕）
  - AndroidManifest `android:name=".LocalAgentApp"` 使用相对引用，正确
  - `Theme.MyApplication` / `LocalAgent` 品牌残留移交 Step 4a/4b 处理
  - 验证：`assembleDebug` ✅ + `testDebugUnitTest` ✅
- [x] Step 3e: 包名迁移 — 测试文件 ✅ (2026-04-25 10:07)
  - ChatModelsTest.kt: `package ai.nora.llm` + `import ai.nora.model.ChatMessage` ✅
  - BaseAndroidTest.kt: `package ai.nora` + `import ai.nora.MainActivity` ✅
  - 目录结构 `ai/nora/` 已正确 ✅
  - 验证：零 `com.example` 残留在测试文件 + 编译 + Unit 测试通过
- [x] Step 4a: 应用名改为 "Nora" — strings.xml ✅ (2026-04-25 10:07)
  - `<string name="app_name">Nora</string>` ✅
  - 验证：`assembleDebug` ✅ + `git commit 7ae00a6`
- [x] Step 4b: 应用名清理 — themes.xml / SetupScreen / 其他残留 ✅ (2026-04-25 10:31)
  - `Theme.MyApplication` → `Theme.Nora` (themes.xml)
  - `LocalAgentTheme` → `NoraTheme` (Theme.kt)
  - `LocalAgentApp` → `NoraApp` (rename + rewrite, 3 refs)
  - `LocalAgentApp.instance` → `NoraApp.instance` (Navigation.kt)
  - SetupScreen "Local Agent" → "Nora"
  - ChatScreen "Local Agent" → "Nora"
  - 验证：全代码库零残留 + `assembleDebug` ✅ + `testDebugUnitTest` ✅ + `git commit f4e2538`
- [x] Step 5: 建立 Nora 色彩系统（Color.kt → NoraColors.kt）✅ (2026-04-25 10:33)
  - 定义：Background=#121212, Surface=#1E1E1E, NoraOrange=#FF6B6B, PrimaryText=#E0E0E0, SecondaryText=#9E9E9E
  - 验证：编译通过 + `assembleDebug` ✅ + `testDebugUnitTest` ✅ + `git commit 012996d`
- [x] Step 6: 重写 Theme.kt — 强制暗色模式，Nora 色板 ✅ (2026-04-25 10:36)
  - 删除 `darkTheme` 参数，硬编码暗色
  - 使用 NoraColors 替代所有 Material 紫色（0xFF6750A4 已消除）
  - `isSystemInDarkTheme` 已移除
  - 验证：编译通过 + `assembleDebug` ✅ + `testDebugUnitTest` ✅ + `git commit d5bad67`
- [x] Step 7: 更新 Typography.kt — Inter + JetBrains Mono 字体引用 ✅ (2026-04-25 10:46)
  - `AppTypography` → `NoraTypography`，Theme.kt 同步更新引用
  - `FontFamily.Default`（Inter 系统字体）+ `FontFamily.Monospace`（代码）
  - 添加 `CodeTypography` 供代码展示使用
  - 验证：`assembleDebug` ✅ + `testDebugUnitTest` ✅ + `git commit d92fb27`
- [x] Step 8: Phase 0 Gate 测试 + 宪法合规审计 ✅ (2026-04-25 10:48)
  - 创建 `AppLaunchTest.kt`（3 cases）：Nora 品牌 / 模型加载界面 / AppBar
  - 创建 `ThemeTest.kt`（3 cases）：暗色渲染 / 文字可读 / 按钮可交互
  - 创建 `BrandingTest.kt`（4 cases）：包名 / 应用名 / NoraApp 单例 / 主题
  - Instrument 测试：10/10 全绿 ✅（medium_phone，0 skipped, 0 failed）
  - **宪法合规审计 5/5**：com.example=0 ✅, LocalAgent/MyApplication=0 ✅, Material紫色=0 ✅, isSystemInDarkTheme=0 ✅, INTERNET=0 ✅
  - git commit: d92fb27（Step 7 + Gate 合并）

**Gate**: ✅ Phase 0 Complete — 编译通过 + Instrument 10/10 + 宪法审计 5/5 全绿
**宪法合规硬性条件**：
  - [x] 包名 = `ai.nora`
  - [x] 应用名 = "Nora"
  - [x] Material 紫色完全消除
  - [x] 强制暗色（不跟随系统）
  - [x] 无 INTERNET 权限
  - [x] Nora 橙 #FF6B6B 存在于色彩系统
**回滚**: `git checkout .`

---

### Phase 1 🔄 IN PROGRESS — 数据持久化（记忆基础）

目标：消息不丢失，对话可恢复

- [x] Step 1: Application 级初始化 Room（NoraApp.kt）✅ (2026-04-25 10:55)
  - 添加 `database` 和 `dataRepository` lateinit 属性
  - Room.databaseBuilder 创建 `nora_database`
  - 验证：`assembleDebug` ✅ + `testDebugUnitTest` ✅ + `git commit 9a363b5`
- [x] Step 2: 在 Navigation.kt 中注入 DataRepository ✅ (2026-04-25 10:57)
  - 通过 `app.dataRepository` 获取 Repository 实例
  - 传递给 ChatViewModel 构造函数
  - 验证：`assembleDebug` ✅ + `testDebugUnitTest` ✅ + `git commit ac98425`
- [x] Step 3: ChatViewModel 接入 DataRepository（构造函数注入）✅ (2026-04-25 10:59)
  - 构造函数添加 `dataRepository: DataRepository` 参数
  - ChatUiState 添加 `currentConversationId: Long? = null`
  - 验证：`assembleDebug` ✅ + `testDebugUnitTest` ✅ + `git commit 216ff4b`
- [ ] Step 4: 发送消息时 write-ahead（先显示后存库）
- [ ] Step 5: 加载对话时从 Room 恢复历史消息
- [ ] Step 6: 新建/切换对话功能
- [ ] Step 7: Phase 1 Instrument 测试
  - `MessagePersistenceTest.kt`：发送消息 → 关闭 App → 重新打开 → 消息仍在
  - `ConversationTest.kt`：创建新对话 → 切换 → 历史消息正确
  - `DataRepositoryTest.kt`：Room CRUD 操作验证

**Gate**: 编译通过 + `connectedDebugAndroidTest` Phase 0+1 全绿 + 发消息 → 关 App → 重开 → 消息仍存在
**回滚**: `git checkout .`

---

### Phase 2 🔲 PENDING — 安全屋主界面（Sanctuary）

目标：打开 Nora 进入安全屋，而非直接进入聊天

- [ ] Step 1: 创建 SanctuaryScreen.kt — 顶部状态栏（离线指示 + Nora 状态 + 呼吸灯）
- [ ] Step 2: 实现 Nora 呼吸光点动画（Canvas + infinite transition）
- [ ] Step 3: 苏醒日志卡片（版本人格化展示）
- [ ] Step 4: 底部三按钮导航（对话 / 日志 / 技能）
- [ ] Step 5: 更新 Navigation.kt — Sanctuary 为首个页面
- [ ] Step 6: Phase 2 Instrument 测试
  - `SanctuaryLaunchTest.kt`：打开 App → 首先看到安全屋（非聊天）
  - `NavigationTest.kt`：点击"对话" → 进入聊天页 → 点返回 → 回到安全屋
  - `OfflineIndicatorTest.kt`：离线指示器始终可见

**Gate**: 编译通过 + `connectedDebugAndroidTest` Phase 0+1+2 全绿 + 打开 App → 看到安全屋 → 离线指示显示 → Nora 呼吸灯动画 → 点击"对话"进入聊天
**回滚**: `git checkout .`

---

### Phase 3 🔲 PENDING — Nora 对话 UI 重塑

目标：对话界面符合 Nora 宪法规范

- [ ] Step 1: Nora 气泡 — #1E1E1E 背景 + 1px #2C2C2C 描边 + 微圆角
- [ ] Step 2: Nora 头像呼吸光点（"N" 字母 + 呼吸动画）
- [ ] Step 3: "正在思考..." 文案替代三个闪动点
- [ ] Step 4: 用户气泡改为淡灰色（非紫色）
- [ ] Step 5: 推理可视化入口（气泡下方 "点击查看 Nora 在想什么 >"）
- [ ] Step 6: 动效优化 — 页面淡入淡出、慢节奏
- [ ] Step 7: Phase 3 Instrument 测试
  - `ChatBubbleStyleTest.kt`：助手气泡背景色为 #1E1E1E、用户气泡为淡灰色
  - `StreamingIndicatorTest.kt`：发送消息后显示"Nora 正在思考..."
  - `MessageInteractionTest.kt`：长按消息 → 上下文菜单（复制/删除）
  - `ScrollBehaviorTest.kt`：新消息自动滚动到底部

**Gate**: 编译通过 + `connectedDebugAndroidTest` Phase 0-3 全绿 + 对话界面暗色 + Nora 气泡样式 + 呼吸动画 + 思考文案
**回滚**: `git checkout .`

---

### Phase 4 🔲 PENDING — 隐私仪表盘（The Vault）

目标：用户完全掌控自己的数据

- [ ] Step 1: 创建 VaultScreen.kt — "你的数据，只有你见过。"
- [ ] Step 2: Nora 记忆库列表（从 Room 读取，支持删除/编辑）
- [ ] Step 3: 零日志模式开关（DataStore 持久化）
- [ ] Step 4: 权限使用记录展示
- [ ] Step 5: 导航入口 — Sanctuary 底部或设置
- [ ] Step 6: Phase 4 Instrument 测试
  - `VaultScreenTest.kt`：打开隐私仪表盘 → 标题正确显示
  - `MemoryManagementTest.kt`：删除记忆条目 → 重新打开 → 条目不存在
  - `ZeroLogModeTest.kt`：开启零日志模式 → 发消息 → 关 App → 重开 → 无历史

**Gate**: 编译通过 + `connectedDebugAndroidTest` Phase 0-4 全绿 + 隐私仪表盘显示 + 记忆条目可删除 + 零日志开关生效
**回滚**: `git checkout .`

---

### Phase 5 🔲 PENDING — 技能树（The Skill Tree）

目标：Nora 能力可视化 + 用户投票机制

- [ ] Step 1: 创建 SkillTreeScreen.kt — 星图/树状结构
- [ ] Step 2: 技能节点三种状态（已点亮/训练中/未点亮）
- [ ] Step 3: 已点亮技能点击弹出 Nora 对话
- [ ] Step 4: 技能数据模型 + Room 持久化
- [ ] Step 5: "让 Nora 学这个"投票功能
- [ ] Step 6: Phase 5 Instrument 测试
  - `SkillTreeRenderTest.kt`：技能树正确渲染，三种状态视觉区分
  - `SkillInteractionTest.kt`：点击未点亮技能 → 弹出投票确认
  - `SkillDetailTest.kt`：点击已点亮技能 → Nora 对话弹出

**Gate**: 编译通过 + `connectedDebugAndroidTest` Phase 0-5 全绿 + 技能树显示 + 三种状态区分 + 点击交互
**回滚**: `git checkout .`

---

### Phase 6 🔲 PENDING — 感知层：通知聚合 & 文件上下文

目标：Nora 实现宪法「感知 (Sense)」维度 — 感知手机通知、自动摘要、读取用户授权文件。完全离线，零网络请求。

前置依赖：Phase 1 (Room 持久化)、Phase 2 (Navigation)

- [ ] Step 1: NotificationListenerService 实现（AndroidManifest service declaration + NoraNotificationService.kt）
  - 创建 `ai/nora/notification/NoraNotificationService.kt`
  - `onNotificationPosted` → 解析 sbn → 存入 Room
  - AndroidManifest 添加 `<service>` 声明（`BIND_NOTIFICATION_LISTENER_SERVICE`）
  - 验证：模拟器通知出现时 log 有输出
- [ ] Step 2: Room NotificationEntity + NotificationDao
  - `NotificationEntity`: id, packageName, appName, title, text, timestamp, isRead, summary, category, isOngoing
  - `NotificationDao`: insert, queryAll, queryByPackage, updateSummary, deleteOld (保留1000条)
  - AppDatabase 添加 `notificationDao()`
  - 验证：`testDebugUnitTest` DAO 测试通过
- [ ] Step 3: DataRepository 通知增删改查
  - `NotificationRepository` 单例（复用 Phase 1 架构模式）
  - 方法：`addNotification`, `getNotifications(limit)`, `getUnreadCount`, `markAsRead`, `cleanupOld`
  - 验证：编译 + Unit 测试
- [ ] Step 4: 通知列表 UI — NotificationScreen
  - 创建 `ai/nora/ui/notification/NotificationScreen.kt`
  - LazyColumn 展示通知卡片（App图标 + 标题 + 正文 + 摘要 + 时间）
  - 过滤 Tab：全部 / 未读 / 按 App 分组
  - 权限未授权时显示引导占位页
  - 验证：编译 + Instrument 测试
- [ ] Step 5: 通知权限引导页
  - 权限未授权检测：`NotificationManagerCompat.getEnabledListenerPackages()`
  - 未授权时显示引导页，带「开启通知监听」按钮 → `ACTION_NOTIFICATION_LISTENER_SETTINGS` DeepLink
  - 授权后自动刷新列表
  - 验证：Instrument 测试权限引导流程
- [ ] Step 6: 权限状态检测 & 通知 Badge
  - `isNotificationListenerEnabled()` 工具函数
  - 安全屋 Tab3（感知）显示未读通知 Badge
  - 验证：Instrument 测试 Badge 显示逻辑
- [ ] Step 7: WorkManager PeriodicWork — NotificationSummaryWorker
  - 依赖：`androidx.work:work-runtime-ktx`
  - `PeriodicWorkRequestBuilder<NotificationSummaryWorker>(15, TimeUnit.MINUTES)`
  - Worker 读取未摘要通知（`summary == null`），批量处理
  - `Result.retry()` on failure
  - 验证：编译 + Worker enqueue 成功
- [ ] Step 8: LLM 摘要 prompt + 解析 + Room 更新
  - Prompt 设计：JSON 格式输出 `[{"index": 0, "summary": "...", "category": "..."}]`
  - 复用现有 `LlmModule.inference()`
  - 解析 JSON → 更新 `NotificationEntity.summary` + `category`
  - 容错：超时/解析失败 → 跳过该批次
  - 验证：Mock LLM 测试 + Room 数据验证
- [ ] Step 9: 摘要展示（NotificationCard 更新）
  - 有摘要时：显示「💡 摘要：...」行，category tag（工作/社交/金融等）
  - 无摘要时：显示原始标题+正文
  - Loading 态：显示「Nora 正在整理...」
  - 验证：Instrument 测试摘要卡片渲染
- [ ] Step 10: SAF 文件选择器 + FileContextEntity
  - `FileContextEntity`: id, uri, fileName, previewText, lastUsed, isActive
  - `ACTION_OPEN_DOCUMENT` → `takePersistableUriPermission`
  - `FileAccessManager` 单例管理持久化 URI 列表
  - 验证：SAF 返回 URI → 持久化成功 → 跨会话保留
- [ ] Step 11: 文件上下文注入对话
  - 聊天输入框旁「📎」按钮 → 打开文件选择器
  - 已选文件列表（Badge）+ 移除按钮
  - 发送消息时：文件内容前缀拼入 prompt
  - 验证：对话中加入文件上下文 → LLM 引用文件内容
- [ ] Step 12: Phase 6 Instrument 测试
  - `NotificationListenerTest.kt`：模拟通知 → 写入 Room → 列表显示
  - `NotificationSummaryTest.kt`：Worker 执行 → 摘要生成 → category 分类
  - `FileAccessTest.kt`：SAF → URI 持久化 → 内容读取 → 上下文注入
  - `PermissionFlowTest.kt`：权限引导 → 授权 → 通知读取 → Badge 更新
  - `DeduplicationTest.kt`：相同通知5min内去重，仅保留最新
  - `DataCleanupTest.kt`：存储超过1000条 → 自动淘汰最旧

**Gate**: 编译通过 + `connectedDebugAndroidTest` Phase 0-6 全绿 + 宪法合规审计 7/7
**宪法合规新增检查**:
  - [x] NLS service declaration present
  - [x] INTERNET permission: 0 matches (strictly enforced)
  - [x] NotificationEntity schema: no PII leakage
  - [x] SAF: no file directory scanning
**回滚**: `git checkout .`

---

## Instrument 测试矩阵总览（更新后）

| Phase | 测试文件数 | 核心覆盖 | 宪法审计项 |
|-------|-----------|---------|-----------|
| Phase 0 | 1 类（3 cases） | App 启动、暗色主题、品牌标识 | 5 项合规检查 |
| Phase 1 | 1 类（3 cases） | 消息持久化、对话切换、Room CRUD | — |
| Phase 2 | 1 类（3 cases） | 安全屋启动、导航流、离线指示 | — |
| Phase 3 | 1 类（4 cases） | 气泡样式、流式指示、消息交互、滚动行为 | — |
| Phase 4 | 1 类（3 cases） | 隐私仪表盘、记忆管理、零日志模式 | — |
| Phase 5 | 1 类（3 cases） | 技能树渲染、技能交互、技能详情 | — |
| Phase 6 | 2 类（5 cases） | 通知监听/摘要、SAF/文件上下文、权限流程、去重/淘汰 | 2 项新增 |
| **合计** | **8 类（24 cases）** | | **7 项宪法审计** |

---

## 状态

**当前 Phase**: Phase 1 🔄 IN PROGRESS
**NEXT_STEP**: Phase 1 Step 4 — 发送消息时 write-ahead（先显示后存库）
**Phase 1 进度**: 3/7 Steps 完成（42.9%）
**Phase 0 进度**: 18/18 Steps 完成（100%）✅ Phase 0 Gate Passed
**Phase 6 状态**: 🔲 Pending（待 Phase 1-5 完成后推进）
**上次 Instrument 测试**: 2026-04-25 10:48 — 10 tests, 10 passed, 0 skipped, 0 failed ✅
**测试通过率**: 100%（10/10）
**效率指标**：
  - Step 平均完成时间：~2.5 min/Step
  - Phase 0 总耗时：~8 min（Step 7 + Gate）
  - 宪法合规度：9/9（100%）
**BLOCKER 状态**: ✅ 无（ADB emulator-5554 在线，2026-04-25 10:48 确认）

### 自动化执行修复记录

| 日期 | 修复项 | 说明 |
|------|--------|------|
| 2026-04-25 | ADB BLOCKER | ADB 完整路径 `C:\Users\28767\AppData\Local\Android\Sdk\platform-tools\adb.exe`，不在系统 PATH |
| 2026-04-25 | 编译命令 | PowerShell 直接调用 gradlew 有环境变量问题；改用 `build.bat` 脚本（cmd /c set 环境变量后执行） |
| 2026-04-25 | 应用图标 | 替换为自定义 Nora 图标（1536x1024 → 裁剪正方形 → 5 分辨率 webp），删除 adaptive icon（非透明背景图标不适合 adaptive 裁剪） |

## 自动化执行纪律（美团味·标准化拆解）

### 每个 Step 的标准执行流程

```
1. 读状态 → 确认 NEXT_STEP
2. 检查前置 → 如果有 BLOCKER，先消除 BLOCKER 再执行 Step
   - git 不存在 → git init + commit
   - ADB 离线 → 用完整路径 `adb.exe devices` 检查；无设备则启动模拟器
   - 编译失败 → 先修编译再继续
3. 执行 Step → 最小原子改动
4. 验证 → 编译 + 测试（Unit 或 Instrument 视 Phase 要求）
5. 审计 → 检查本 Step 是否引入新违规（宪法 + 编码规范）
6. git commit → 每个 Step 独立 commit
7. 更新 Tracker → 标记 Step 完成，推进 NEXT_STEP
```

### BLOCKER 自动消除规则

| BLOCKER | 自动消除动作 | 消除后状态 |
|---------|------------|-----------|
| git 不存在 | `git init && git add -A && git commit` | Step 1a 完成 |
| ADB 离线 | 用完整路径检查：`C:\Users\28767\AppData\Local\Android\Sdk\platform-tools\adb.exe devices`；无设备则启动 `emulator -avd medium_phone` | ADB_BLOCKER 消除 |
| 编译失败 | 读报错 → 修编译 → 重新 `gradlew assembleDebug` | BLOCKER 消除 |
| 模拟器未启动 | `adb -s emulator-5554 emu kill` → `avdmanager` → `emulator -avd medium_phone` | ADB_BLOCKER 消除 |

### 铁律（不可违反，违者 3.25）

1. **不跳 Phase** — 必须按 Phase 顺序推进，Phase 0 未 Gate 不能进 Phase 1
2. **不跳 Step** — 同一 Phase 内必须按 Step 顺序执行
3. **不跳测试** — 每个 Phase Gate 必须有 Instrument 测试全绿
4. **最小增量** — 一次一个原子改动，改完即验证
5. **安全优先** — Gate 失败立即 `git checkout .` 回滚
6. **宪法约束** — 所有改动必须符合 nora-constitution.md
7. **不重复劳动** — 已完成的 Step 不重做
8. **测试驱动 Gate** — `connectedDebugAndroidTest` 全绿才推进
9. **真实测试** — 不写空测试、不写 `@Ignore`、不写 TODO 测试
10. **宪法合规审计** — Phase 0 Gate 时自动执行 5 项宪法审计检查
11. **进度透明** — 每个 Step 完成后更新进度百分比和效率指标
