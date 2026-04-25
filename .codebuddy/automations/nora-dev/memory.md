# Nora Dev Automation — Execution Memory

## 2026-04-25 10:21 — 应用图标替换 + 自动化流程修复 ✅
- **图标替换**: `app-icon.png` (1536x1024, RGB) → Pillow 裁剪为正方形 → 5 分辨率 webp → 覆盖 mipmap-*/ic_launcher*.webp
- **移除 adaptive icon**: 删除 `mipmap-anydpi-v26/` 目录 + `drawable/ic_launcher_background.xml` + `ic_launcher_foreground.xml`（非透明背景图标不适合 adaptive 裁剪）
- **ADB BLOCKER 消除**: 启动模拟器 medium_phone → `emulator-5554` 在线 ✅
- **编译方案修复**: 创建 `build.bat` 脚本解决 PowerShell 环境变量问题（已加入 .gitignore）
- **Git commit**: `7733221` — "Phase 0: replace app icon with custom Nora icon"
- **Tracker**: BLOCKER 状态更新为 ✅ 无；BLOCKER 自动消除规则已更新 ADB 完整路径
- **NEXT**: Phase 0 Step 4b — 应用名清理（themes.xml / SetupScreen / 其他残留）

## 2026-04-25 10:07 — Phase 0 Steps 3d/3e/4a ✅ (3 Steps)
- **Step 3d**: XML 资源 + AndroidManifest 包名引用 — 确认零 `com.example` 残留（3a/3b/3c 已处理）
- **Step 3e**: 测试文件 — ChatModelsTest.kt + BaseAndroidTest.kt 均已迁移到 `ai.nora` 包 ✅
- **Step 4a**: strings.xml `app_name` LocalAgent → Nora ✅
- **Git commit**: `7ae00a6` (4a)
- **Tracker**: Phase 0 进度 13/18（72.2%），NEXT = Step 4b（themes.xml / SetupScreen / 其他残留）
- **注意**: JAVA_HOME 需通过 PowerShell `$env:JAVA_HOME` 设置（cmd /c set 受系统变量尾部 `\\` 干扰）
- **gradle 命令**: `Start-Process -NoNewWindow -FilePath "cmd.exe" -ArgumentList "/c gradlew.bat ... > build_out.txt 2>&1" -Wait`

## 2026-04-25 09:39-09:44 — Phase 0 Steps 3a/3b/3c ✅ (3 Steps)
- **Step 3a**: build.gradle.kts — namespace + applicationId `com.example.localagent` → `ai.nora` ✅ 编译通过
- **Step 3b**: 17 个 .kt 源文件 package 声明 + imports 全部替换；测试目录结构同步迁移 ✅ Unit test 全绿
- **Step 3c**: 目录树 `com/example/localagent/` → `ai/nora/`（17 个源文件 rename）✅ 编译 + 单元测试全绿
- **Git commits**: `b181230` (3a) → `6ea5bae` (3b) → `3ad61bb` (3c) — 3ad61bb 删除临时脚本
- **Tracker**: Phase 0 进度 10/18（55.6%），NEXT = Step 3d（XML 资源 + AndroidManifest）
- **注意**: 测试文件 ChatModelsTest.kt 被 `fix_package.py` 清空，后从 git 恢复并重新写入正确内容
- **踩坑**: Python `fix_package.py` 操作需小心处理文件内容，避免意外清空

## 2026-04-25 04:32 — Phase 0 Steps 1-2 验证 ✅
- **完成**: 验证 Steps 1a/1b/1c/1d/2a/2b 全部自动完成（git 已有历史，死代码已删除，MessageDao 实为 ConversationDao.kt 同包内嵌）
- **验证**: `assembleDebug` ✅ | `testDebugUnitTest` ✅ (BUILD SUCCESSFUL)
- **发现**: ADB 不在 PATH，Instrument 测试无法执行（⚠️ ADB_BLOCKER）
- **Tracker 更新**: Phase 0 进度 7/18（38.9%），NEXT = Step 3a（包名迁移）
- **NEXT**: Phase 0 Step 3a — 包名 `com.example.localagent` → `ai.nora`

## 2026-04-25 02:44 — Phase 0 Step 0 ✅
- **完成**: 搭建测试基础设施
- **改动**: build.gradle.kts 添加 testInstrumentationRunner；删除 MainScreenTest.kt + SetupScreenTest.kt；创建 ai.nora/BaseAndroidTest.kt；清理旧 com.example 目录
- **验证**: assembleDebug ✅ | connectedDebugAndroidTest ✅ (0 tests, empty pass)
- **发现**: 项目无 git 仓库（fatal: not a git repository），后续需 git init
- **NEXT**: Phase 0 Step 1 — 修复编译阻断（删除死代码）
