# .harness Workflow — 开发步骤执行模板
# 这是编排层的核心工作流，每个开发步骤都按此执行

## 输入
- step_description: 步骤描述
- phase: 当前阶段
- context: 相关上下文

## 执行流程

### 1. 记忆加载 [memory_agent]
```
LOAD .harness/memory/context.yaml      → 项目全量上下文
LOAD .harness/memory/patterns.md       → 已验证模式
LOAD .harness/memory/pitfalls.md       → 已知坑点
LOAD .harness/memory/decisions.md      → 架构决策
LOAD .workbuddy/memory/MEMORY.md       → 项目记忆索引
LOAD .workbuddy/memory/nora-phase-tracker.md → 阶段进度
FILTER relevance TO current_step
OUTPUT context_summary
```

### 2. 步骤解析 [orchestration_agent]
```
READ step_description
IDENTIFY:
  - 要创建的文件
  - 要修改的文件
  - 依赖的现有代码
  - 验收标准
OUTPUT action_plan
```

### 3. 实现 [coding_agent]
```
FOR EACH file IN action_plan:
  READ existing content (if modify)
  APPLY known_pitfalls check
  WRITE code
  SELF_CHECK:
    - 零网络约束
    - MVVM 架构
    - Material3 组件
    - 已知坑点规避
```

### 4. 编译验证 [build_agent]
```
RUN build.bat
IF compile_error:
  PARSE error → file:line: message
  RETURN to coding_agent with error context
  RETRY (max 3)
IF success:
  CONTINUE
```

### 5. 测试 [test_agent] (如适用)
```
IF step涉及可测试逻辑:
  RUN gradlew.bat test
  RUN gradlew.bat connectedAndroidTest (如 AVD 可用)
  PARSE results
  IF failure:
    RETURN to coding_agent with failure context
```

### 6. 提交 [git_agent]
```
git add <changed_files>
git commit -m "<step>: <description>"
UPDATE .workbuddy/memory/nora-phase-tracker.md
```

### 7. 反馈记录 [feedback_agent]
```
APPEND .harness/feedback/metrics.yaml:
  timestamp: <now>
  phase: <current>
  step: <step_id>
  status: success|failure
  duration: <seconds>
  files_changed: [<list>]
  commit: <hash>
```

## 错误处理

### 编译失败
- 最多重试 3 次
- 每次将错误详情传递给 coding_agent
- 3 次后仍未解决 → 记录到 feedback/reviews/ 并通知用户

### 测试失败
- 最多重试 2 次
- 失败的测试用例和原因传递给 coding_agent
- 2 次后 → 记录到 feedback/reviews/ 并通知用户

### Git 冲突
- 不自动解决冲突
- 记录到 feedback/reviews/ 并通知用户手动处理
