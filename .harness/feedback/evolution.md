# .harness Feedback Layer — 自演化追踪
# 记录框架自身的演进历程

## 演化记录

### v0.1.0 — 2026-04-25 | 初始创建
- 创建四层架构：记忆、编排、执行、反馈
- 注册 12 个 Agent
- 定义 6 条管线：dev_step, requirement, github_issue, review, evolve
- 集成 GitHub CLI
- 初始任务注册表包含 13 个可执行任务

## 待优化项

- [ ] 实现 Agent 间的自动重试和降级逻辑
- [ ] 添加管线执行的可视化 dashboard
- [ ] 实现并行 stage 执行（当前全串行）
- [ ] 添加自动 code review 的 diff 解析
- [ ] 集成 AVD 自动启停
- [ ] 实现 metrics 的趋势分析和报告生成
- [ ] 添加管线执行的 Webhook 回调

## 自演化策略

### 触发条件
1. 管线连续失败 2+ 次 → 分析根因，更新 pitfalls.md
2. 某 stage 平均耗时 > 阈值 → 优化执行策略
3. 新 pattern 被验证有效 → 追加到 patterns.md
4. 用户反馈 → 更新 agents.yaml 中的 prompt_template

### 演化流程
1. feedback_agent 收集 metrics
2. self_evolve_agent 分析趋势
3. 更新 memory 层文件
4. 优化 pipeline 定义
5. 提交变更到 git
