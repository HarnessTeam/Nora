# MEMORY.md — 长期记忆

## Nora Android 项目（C:\Users\28767\WorkBuddy\local-agent）

### 项目概述
- **产品**: Nora — 离线 Android AI 智能体（数字生命）
- **包名**: `ai.nora`
- **Phase 状态**: Phase 0 ✅ Complete（100%，18/18 Steps + Gate passed），推进 Phase 1
- **技术栈**: Kotlin + Jetpack Compose + Room + ExecuTorch + Navigation3

### 当前 Phase 0 进度
- ✅ 全部 18 Steps 完成
- ✅ Phase 0 Gate: Instrument 10/10 passed, 宪法合规审计 5/5 passed
- ✅ git commit d92fb27
- **NEXT_STEP**: Phase 1 Step 1 — Application 级初始化 Room（NoraApp.kt）
- **Phase 6** 🔲 Pending：通知聚合 & 文件上下文（12 Steps，宪法感知维度落地）

### Phase 6 概要（感知层）
- 目标：Nora 实现「感知 (Sense)」宪法维度 — 通知监听 + 自动摘要 + 文件上下文
- 核心技术：NotificationListenerService + WorkManager PeriodicWork + SAF + Qwen3 LLM
- 依赖：Phase 1 (Room) → Phase 2 (Navigation) → Phase 6
- 关键约束：INTERNET 禁令（宪法红线）、Android 15 OTP 过滤（接受）、Room ≤1000 条
- 调研报告：`.workbuddy/memory/nora-notification-deep-research.md`

### 踩坑记录
- FontFamily("String") 在某些 Compose 版本报错 "expected Boolean"：改用 FontFamily.Default / FontFamily.Monospace 替代

### 环境
- JDK: `C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot`
- Gradle opts: `-Xmx1536m`
- ADB: `C:\Users\28767\AppData\Local\Android\Sdk\platform-tools\adb.exe`（不在 PATH，需用完整路径）
- 模拟器: medium_phone（API 36, x86_64）
- 编译方案: `build.bat` 脚本（PowerShell 直接调 gradlew 有环境变量问题）

### 踩坑记录
- MessageDao: 不存在独立文件，实为 `ConversationDao.kt` 同包内嵌的 DAO 接口
- Type.kt / Color.kt: 已在之前 Step 中删除，无需重复操作
- MainScreenViewModelTest.kt: 已删除，无需操作
- 应用图标: 已替换为自定义 Nora 图标（git commit 7733221），删除了 adaptive icon（mipmap-anydpi-v26）
