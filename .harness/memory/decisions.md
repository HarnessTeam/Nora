# .harness Memory Layer — 架构决策日志
# 每次重大技术决策都记录在此，供未来参考

## 格式
每个决策包含：日期、决策内容、备选方案、理由、影响范围

---

## 2026-04-25 | 创建 .harness 自动化框架

**决策**: 构建四层自动化框架（记忆→编排→执行→反馈）

**备选方案**:
1. 延续 .codebuddy 的 ad-hoc 方式
2. 使用 GitHub Actions CI/CD
3. 自建全链路框架

**选择**: 方案 3 — 自建 .harness

**理由**:
- .codebuddy 缺乏结构化编排和反馈循环
- GitHub Actions 依赖网络，违反零网络约束
- 自建框架可以深度适配离线 Android 开发流程
- 可与 Claude Code 的 Agent 系统无缝集成

**影响**: 所有自动化任务统一走 .harness 调度

---

## 2026-04-25 | ExecuTorch 作为推理引擎

**决策**: 使用 ExecuTorch + Qwen3-0.6B

**理由**: Meta 开源，支持移动端部署，模型体积适合 bundled flavor

**影响**: 需要 SoLoader + fbjni 依赖，JNI 层有崩溃风险
