# MEMORY.md — 长期记忆

## Nora Android 项目（C:\Users\28767\WorkBuddy\local-agent）

### 项目概述
- **产品**: Nora — 离线 Android AI 智能体（数字生命）
- **包名**: `com.example.localagent`（迁移中 → `ai.nora`）
- **Phase 状态**: Phase 0 进行中（72.2%，Step 4b 待执行）
- **技术栈**: Kotlin + Jetpack Compose + Room + ExecuTorch + Navigation3

### 当前 Phase 0 进度
- ✅ Step 0: 测试基础设施
- ✅ Step 1a: Git 初始化
- ✅ Step 1b/1c/1d: 死代码清理
- ✅ Step 2a/2b: DataRepository + AppDatabase 验证
- ✅ Step 3a: build.gradle.kts namespace + applicationId → ai.nora
- ✅ Step 3b: 源文件 package 声明 + imports 替换（17 文件）
- ✅ Step 3c: 目录结构 com.example.localagent → ai.nora（rename）
- ✅ Step 3d: XML 资源和 AndroidManifest 包名引用（已确认零残留）
- ✅ Step 3e: 测试文件引用更新（ChatModelsTest + BaseAndroidTest）
- ✅ Step 4a: 应用名 "LocalAgent" → "Nora"（strings.xml）
- ⬜ Step 4b: 应用名清理 — themes.xml / SetupScreen / 其他残留
- ⬜ Step 5-7: Nora 色彩/Theme/Typography
- ⬜ Step 8: Phase 0 Gate（宪法合规审计）

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
