# .harness Memory Layer — 已验证模式
# 记录成功经验和可复用的开发模式

## Android/Compose 开发模式

### Room 数据库
- Entity → DAO → Database 三件套，KSP 自动生成
- migration 必须在 Database builder 中注册
- 测试用 inMemoryDatabaseBuilder 避免污染

### Navigation3
- 使用 BackStack 管理页面栈
- 初始必须 push 默认路由，否则白屏
- deep link 通过 NavDeepLink 处理

### Compose UI
- State hoisting 到 ViewModel
- SideEffect 放 LaunchedEffect
- preview 需要 mock ViewModel（用 remember + stub state）

### ExecuTorch 推理
- 模型加载是异步的，需要 loading 状态
- tokenizer 和 model 必须配对
- 推理结果通过 callback/flow 回传 UI

## 自动化模式

### Phase 推进
1. 读 phase-tracker 获取当前状态
2. 执行下一步骤
3. 编译验证（build.bat）
4. 如有测试，运行测试
5. git commit + 更新 tracker

### Git 工作流
- 每步一个 commit，message 格式: `step: 描述`
- 不推送到 remote 除非用户要求或 cron 触发
- commit 前检查 git status 避免遗漏文件
