# .harness Memory Layer — 已知坑点
# 记录踩过的坑，避免重复犯错

## 编译相关

### FontFamily 构造
- **问题**: `FontFamily(listOf(Font(...)))` 在某些 Compose 版本报错
- **解决**: 使用 `FontFamily.Default` 或通过 XML resource 引用
- **严重度**: 编译失败

### build.bat 限制
- **问题**: 脚本内硬编码 variant，不支持命令行参数透传
- **解决**: 直接调用 `gradlew.bat assemble{Variant}` 或修改 build.bat
- **严重度**: 构建变体切换不便

### JDK 路径
- **问题**: Windows 上 JAVA_HOME 含空格（Program Files）
- **解决**: build.bat 中用引号包裹路径
- **严重度**: 构建失败

## 运行时相关

### Navigation3 白屏
- **问题**: BackStack 初始为空时界面全白
- **解决**: 在 NavHost 初始化时 push 默认路由
- **严重度**: 用户体验差

### ExecuTorch JNI 崩溃
- **问题**: 模型文件缺失或格式不匹配导致 native crash
- **解决**: 加载前校验文件大小和 SHA，try-catch JNI 调用
- **严重度**: 应用崩溃（hs_err_pid*.log）

### Compose Preview
- **问题**: Preview 不支持 Hilt 注入和 ExecuTorch
- **解决**: 创建 Preview 专用的 stub ViewModel
- **严重度**: 开发效率降低

## 自动化相关

### Cron 任务并发
- **问题**: 30 分钟 cron 可能在上次任务未完成时触发
- **解决**: scheduled_tasks.lock 做互斥检查
- **严重度**: 状态冲突

### Git merge 冲突
- **问题**: 自动 commit 可能与手动修改冲突
- **解决**: 自动任务只修改 phase-tracker 和新增代码，不修改已有文件
- **严重度**: 需要手动解决
