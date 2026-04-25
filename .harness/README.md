# .harness — Nora 自动化开发框架

> 你提需求，我来实现。四层全链路自动化。

## 架构

```
.harness/
├── agent.md                         ← 总编排入口（你跟它说需求）
├── memory/                          ← 记忆层
│   ├── agent.md                        自主记忆 Agent
│   ├── context.yaml                    项目全量上下文
│   ├── patterns.md                     已验证的开发模式
│   ├── pitfalls.md                     已知坑点
│   └── decisions.md                    架构决策日志
├── orchestration/                   ← 编排层
│   ├── agent.md                        自主编排 Agent
│   ├── pipeline.yaml                   管线定义（6条管线）
│   ├── agents.yaml                     Agent 配置（12个Agent）
│   └── workflows/
│       └── dev-step.md                 开发步骤工作流模板
├── execution/                       ← 执行层
│   ├── agent.md                        自主执行 Agent
│   ├── tasks/
│   │   └── registry.yaml              任务注册表（13个任务）
│   └── scripts/
│       └── harness-run.sh             Shell 执行入口
└── feedback/                        ← 反馈层
    ├── agent.md                        自主反馈 Agent
    ├── metrics.yaml                    执行指标记录
    ├── evolution.md                    自演化追踪
    └── reviews/
        └── README.md                   审查记录目录
```

## 四层架构

| 层级 | 职责 | 核心文件 |
|------|------|----------|
| **记忆层** | 加载上下文、模式、坑点 | `memory/context.yaml` |
| **编排层** | 选管线、拆任务、调度执行 | `orchestration/pipeline.yaml` |
| **执行层** | 写代码、编译、测试、Git | `execution/tasks/registry.yaml` |
| **反馈层** | 记录指标、分析、自演化 | `feedback/metrics.yaml` |

## 快速使用

### 在 Claude Code 中使用
直接对 Claude 说需求，它会自动调用 .harness 框架：

```
"帮我实现 Phase 1 Step 7 的 Instrument 测试"
"添加一个新功能：对话导出为 TXT"
"编译一下看有没有问题"
"帮我提一个 issue 描述 XXX Bug"
"自动推进，继续开发下一步"
```

### 命令行使用
```bash
# 编译
bash .harness/execution/scripts/harness-run.sh build bundledDebug

# 测试
bash .harness/execution/scripts/harness-run.sh test unit

# Git 操作
bash .harness/execution/scripts/harness-run.sh git status

# GitHub 操作
bash .harness/execution/scripts/harness-run.sh github issue-list
```

## 管线清单

| 管线 | 用途 | 触发方式 |
|------|------|----------|
| `dev_step` | 执行单个开发步骤 | "执行下一步" |
| `requirement` | 从需求到交付 | "帮我实现 XXX" |
| `github_issue` | Issue 驱动开发 | "从 issue #N 开发" |
| `review` | 代码审查 | "审查当前改动" |
| `evolve` | 框架自演化 | "优化框架" |

## 设计原则

1. **用户零负担**：只需提自然语言需求
2. **四层协作**：记忆→编排→执行→反馈，全链路自动化
3. **持续进化**：每次执行都让框架更聪明（反馈层驱动自演化）
4. **离线优先**：所有操作在本地完成，不依赖网络
5. **安全可控**：不做 destructive 操作，遇到歧义询问用户
