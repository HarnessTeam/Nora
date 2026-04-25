# MEMORY.md — 长期记忆

## Nora Android 项目（C:\Users\28767\WorkBuddy\local-agent）

### 项目概述
- **产品**: Nora — 离线 Android AI 智能体（数字生命）
- **包名**: `com.example.localagent`（迁移中 → `ai.nora`）
- **Phase 状态**: Phase 0 进行中（38.9%，Step 3a 待执行）
- **技术栈**: Kotlin + Jetpack Compose + Room + ExecuTorch + Navigation3

### 当前 Phase 0 进度
- ✅ Step 0: 测试基础设施
- ✅ Step 1a: Git 初始化
- ✅ Step 1b/1c/1d: 死代码清理
- ✅ Step 2a/2b: DataRepository + AppDatabase 验证
- ⬜ Step 3a-3e: 包名迁移（com.example.localagent → ai.nora）
- ⬜ Step 4a/4b: 应用名清理
- ⬜ Step 5-7: Nora 色彩/Theme/Typography
- ⬜ Step 8: Phase 0 Gate（宪法合规审计）

### 环境
- JDK: `C:\Program Files\Eclipse Adoptium\jdk-17.0.18.8-hotspot`
- Gradle opts: `-Xmx1536m`
- ADB: `C:\Users\28767\AppData\Local\Android\Sdk\platform-tools\adb.exe`（⚠️ 不在 PATH）
- 模拟器: medium_phone（API 36, x86_64）

### 踩坑记录
- MessageDao: 不存在独立文件，实为 `ConversationDao.kt` 同包内嵌的 DAO 接口
- Type.kt / Color.kt: 已在之前 Step 中删除，无需重复操作
- MainScreenViewModelTest.kt: 已删除，无需操作
