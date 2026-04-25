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
# 环境变量
set GRADLE_OPTS=-Xmx1536m
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot

# Unit 测试（快速反馈）
gradlew testDebugUnitTest

# Instrument 测试（需要设备/模拟器）
gradlew connectedDebugAndroidTest

# 单个测试类
gradlew connectedDebugAndroidTest --tests "ai.nora.ui.chat.ChatScreenTest"
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
- [ ] Step 4b: 应用名清理 — themes.xml / SetupScreen / 其他残留
  - `Theme.MyApplication` → `Theme.Nora`
  - `LocalAgentTheme` → `NoraTheme`
  - `LocalAgentApp` → `NoraApp`
  - SetupScreen "Local Agent" → "Nora"
  - 验证：全代码库无 "LocalAgent" / "MyApplication" 残留
- [ ] Step 5: 建立 Nora 色彩系统（Color.kt → NoraColors.kt）
  - 定义：Background=#121212, Surface=#1E1E1E, NoraOrange=#FF6B6B, PrimaryText=#E0E0E0, SecondaryText=#9E9E9E
  - 验证：编译通过
- [ ] Step 6: 重写 Theme.kt — 强制暗色模式，Nora 色板
  - 删除 `darkTheme` 参数，硬编码暗色
  - 使用 NoraColors 替代所有 Material 紫色
  - 验证：编译通过
- [ ] Step 7: 更新 Typography.kt — Inter + JetBrains Mono 字体引用
  - 验证：编译通过
- [ ] Step 8: Phase 0 Gate 测试 + 宪法合规审计
  - `AppLaunchTest.kt`：App 启动显示 "Nora" 标题
  - `ThemeTest.kt`：暗色模式强制生效（背景色 #121212）
  - `BrandingTest.kt`：包名为 `ai.nora`，应用名 "Nora"
  - **宪法合规审计**（自动化执行）：
    1. `grep -r "com.example" app/src/` → 0 匹配
    2. `grep -r "LocalAgent\|MyApplication" app/src/` → 0 匹配
    3. `grep -r "0xFF6750A4\|0xFFD0BCFF\|Purple" app/src/main/` → 0 匹配（Material 紫色消除）
    4. `grep -r "isSystemInDarkTheme" app/src/main/` → 0 匹配（强制暗色，不跟随系统）
    5. `grep "INTERNET" app/src/main/AndroidManifest.xml` → 0 匹配（离线优先）
  - 验证：`connectedDebugAndroidTest` 全绿 + 宪法审计 5 项全通过
  - git commit: "Phase 0 complete: Nora branding + dark theme + test gate"

**Gate**: 编译通过 + `connectedDebugAndroidTest` Phase 0 全绿 + 宪法合规审计 5 项全通过
**宪法合规硬性条件**：
  - [ ] 包名 = `ai.nora`
  - [ ] 应用名 = "Nora"
  - [ ] Material 紫色完全消除
  - [ ] 强制暗色（不跟随系统）
  - [ ] 无 INTERNET 权限
  - [ ] Nora 橙 #FF6B6B 存在于色彩系统
**回滚**: `git checkout .`

---

### Phase 1 🔲 PENDING — 数据持久化（记忆基础）

目标：消息不丢失，对话可恢复

- [ ] Step 1: Application 级初始化 Room（LocalAgentApp.kt → NoraApp.kt）
- [ ] Step 2: 在 Navigation.kt 中注入 AppDatabase + DataRepository
- [ ] Step 3: ChatViewModel 接入 DataRepository（构造函数注入）
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

## Instrument 测试矩阵总览

| Phase | 测试文件数 | 核心覆盖 | 宪法审计项 |
|-------|-----------|---------|-----------|
| Phase 0 | 1 类（3 cases） | App 启动、暗色主题、品牌标识 | 5 项合规检查 |
| Phase 1 | 1 类（3 cases） | 消息持久化、对话切换、Room CRUD | — |
| Phase 2 | 1 类（3 cases） | 安全屋启动、导航流、离线指示 | — |
| Phase 3 | 1 类（4 cases） | 气泡样式、流式指示、消息交互、滚动行为 | — |
| Phase 4 | 1 类（3 cases） | 隐私仪表盘、记忆管理、零日志模式 | — |
| Phase 5 | 1 类（3 cases） | 技能树渲染、技能交互、技能详情 | — |
| **合计** | **6 类（19 cases）** | | **5 项宪法审计** |

---

## 状态

**当前 Phase**: 0（项目重生）
**NEXT_STEP**: Phase 0 Step 4b — 应用名清理：themes.xml / SetupScreen / 其他残留
**Phase 0 进度**: 13/18 Steps 完成（72.2%）
**上次 Instrument 测试**: 2026-04-25 02:44 — 0 tests, BUILD SUCCESSFUL
**测试通过率**: 100%（空跑，0/0）
**效率指标**：
  - Step 平均完成时间：~3 min/Step
  - Session 内 Step 吞吐量：4 Steps in ~10 min（0.4 Steps/min）
  - 宪法合规度：4/9（44%，目标 Phase 0 完成后 7/9）
**BLOCKER 状态**：⚠️ ADB 不在 PATH，Instrument 测试待补

## 自动化执行纪律（美团味·标准化拆解）

### 每个 Step 的标准执行流程

```
1. 读状态 → 确认 NEXT_STEP
2. 检查前置 → 如果有 BLOCKER，先消除 BLOCKER 再执行 Step
   - git 不存在 → git init + commit
   - ADB 离线 → 标记 BLOCKER，跳过测试
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
| ADB 离线 | 跳过 Instrument 测试，标记 ⚠️ ADB_BLOCKER | Step 可继续，测试待补 |
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
