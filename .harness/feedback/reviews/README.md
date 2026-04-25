# .harness Feedback Layer — 审查记录
# 每次失败或需要人工介入的执行在此生成审查文件

## 文件命名规则
- `{date}-{pipeline}-{step}.md` — 单次执行审查
- `{date}-review.md` — 代码审查报告

## 示例

### 2026-04-25-dev-step-compile.md
```markdown
# 编译失败审查

- 日期: 2026-04-25
- 管线: dev-step
- 阶段: compile
- 错误: Unresolved reference: FontFamily
- 文件: app/src/main/java/ai/nora/ui/theme/Type.kt:15
- 根因: 使用了 FontFamily(string) 构造器，Compose 不支持
- 修复: 改用 FontFamily.Default
- 状态: 已修复
```
