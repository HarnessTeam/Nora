# Nora 通知聚合 & 文件读取 — 需求规格

## 1. 背景

Nora 作为**本地离线 AI 智能体**，核心价值在于"无云干预、隐私优先"。用户期望 Nora 能：

- 实时感知手机上发生的一切（通知）
- 主动整理、归纳、摘要信息
- 读取本地文件作为上下文

这三点是数字生命的基础能力。

---

## 2. 核心功能

### F1: 通知监听 & 聚合

| 维度 | 规格 |
|------|------|
| 监听范围 | 所有 App 的所有通知（需 NotificationListenerService 授权） |
| 数据字段 | packageName, appName, title, text, timestamp, isOngoing, extras |
| 去重策略 | 相同 title+text 在 5min 内仅保留最新一条 |
| 存储 | Room: `NotificationEntity`（含 isRead, summary, category） |
| 后台处理 | WorkManager PeriodicWork（每 15min 批量 LLM 摘要） |

### F2: LLM 自动摘要

| 维度 | 规格 |
|------|------|
| 模型 | 复用现有 Qwen3-0.6B 量化（`/data/local/tmp/llama/model.pte`） |
| 触发时机 | 通知积累 ≥5 条时、或用户主动触发 |
| 摘要 prompt | `"请归纳以下通知，每条用一句话概括：[通知列表]"` |
| 输出 | 存储到 `NotificationEntity.summary`，UI 展示摘要卡片 |
| 离线 | 完全本地，**无网络请求** |

### F3: 文件读取 & 上下文注入

| 维度 | 规格 |
|------|------|
| 文件类型 | .txt / .md / .json / .pdf（初期仅文本） |
| 权限模型 | SAF (Storage Access Framework) — 用户主动选择文件，Nora 获得 URI 权限 |
| 权限持久化 | 通过 `ContentResolver.takePersistableUriPermission` 保留跨会话访问 |
| 上下文注入 | 文件内容拼接为 prompt 前缀，送入 LLM 对话 |
| 隐私 | 不扫描文件目录，只读用户明确授权的文件 |

---

## 3. 权限清单

| 权限 | 用途 | 申请时机 |
|------|------|----------|
| `BIND_NOTIFICATION_LISTENER_SERVICE` | 通知读取 | 设置引导页，用户主动授权 |
| `READ_EXTERNAL_STORAGE` (API < 33) | 文件读取（兼容） | SAF 触发时系统自动申请 |
| `POST_NOTIFICATIONS` (API ≥ 33) | Nora 自身通知 | 设置引导页 |
| `FOREGROUND_SERVICE` | WorkManager 后台摘要 | 首次后台任务前 |
| `INTERNET` | **禁止** | 纯离线，宪法红线 |

> ⚠️ `INTERNET` 权限在 Phase 0 宪法中已排除，这里再次强调：整个 F1-F3 零网络请求。

---

## 4. 数据模型（Room）

```kotlin
@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,          // 来源 App
    val appName: String,              // 来源 App 中文名
    val title: String,                // 通知标题
    val text: String,                 // 通知正文
    val timestamp: Long,             // 通知时间（epoch ms）
    val isRead: Boolean = false,     // 是否已读
    val summary: String? = null,     // LLM 摘要
    val category: String? = null,     // LLM 自动分类：工作/社交/金融...
    val isOngoing: Boolean = false
)

@Entity(tableName = "file_contexts")
data class FileContextEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uri: String,                  // persistable URI
    val fileName: String,
    val previewText: String,          // 前 512 字符预览
    val lastUsed: Long,              // 最后使用时间
    val isActive: Boolean = true      // 是否作为上下文注入
)
```

---

## 5. UI/UX

### 5.1 设置引导（首次启动）

```
[通知权限引导]
  Nora 需要读取通知才能帮你整理信息。
  [开启通知监听权限] → 跳转系统设置 → NotificationListener
  [暂不开启] → 跳过
```

### 5.2 通知聚合 Tab

```
[通知聚合]
  ┌─────────────────────────────────┐
  │ 🔔 今日 23 条通知               │
  │ [全部摘要] [仅未读] [按 App]    │
  ├─────────────────────────────────┤
  │ 📦 快递  10:30                  │
  │ 您的包裹已到达菜鸟驿站...        │
  │ 💡 摘要：韵达快递提醒取件        │
  ├─────────────────────────────────┤
  │ 💬 微信  09:15  [未读]         │
  │ [图片]                          │
  ├─────────────────────────────────┤
  │ 📱 系统  08:00                  │
  │ 存储空间不足通知...             │
  └─────────────────────────────────┘
```

### 5.3 文件上下文

```
[文件上下文]
  已加载文件（拖入对话自动注入）:
  ├─ project_spec.md ✓
  ├─ notes_2024.txt ✓
  └─ [+] 添加文件（SAF 文件选择器）
```

---

## 6. 技术架构

```
┌─────────────────────────────────────────────────┐
│                    Nora App                       │
├─────────────────────────────────────────────────┤
│  NotificationListenerService                     │
│  (系统绑定 → 监听所有通知 → 写 Room)             │
├─────────────────────────────────────────────────┤
│  WorkManager PeriodicWork                        │
│  (每 15min → 读取未摘要通知 → LLM → 更新 Room)   │
├─────────────────────────────────────────────────┤
│  FileAccessManager (SAF)                         │
│  (takePersistableUriPermission → 持久 URI)       │
├─────────────────────────────────────────────────┤
│  LLM InferenceEngine (ExecuTorch/Qwen3)          │
│  (通知摘要 / 文件内容理解)                        │
├─────────────────────────────────────────────────┤
│  Room Database                                   │
│  (NotificationEntity + FileContextEntity)        │
└─────────────────────────────────────────────────┘
```

---

## 7. 实现计划

### Phase 1: 通知聚合（基础）
1. NotificationListenerService 实现
2. Room NotificationEntity + DAO
3. DataRepository 增删改查
4. 通知列表 UI（Compose LazyColumn）
5. 权限引导页

### Phase 2: LLM 摘要 + 文件上下文
6. WorkManager PeriodicWork
7. LLM 摘要 prompt + 解析
8. SAF 文件选择器
9. 文件上下文注入对话
10. Phase Gate: Instrument 全绿 + 宪法审计

---

## 8. 验收标准

- [ ] NotificationListenerService 成功读取至少 2 个测试 App 通知
- [ ] Room 存储 ≥10 条通知，去重生效
- [ ] LLM 摘要生成成功率 ≥80%（不 crash）
- [ ] SAF 文件选择器正常返回 URI，持久化生效
- [ ] 通知 Tab UI 加载 <500ms（100 条以内）
- [ ] **宪法红线**：整个 Phase 1-2 代码中 `INTERNET` 零出现

---

## 9. 风险 & 对策

| 风险 | 级别 | 对策 |
|------|------|------|
| NotificationListener 被用户禁用 | P1 | 引导页强提示，Settings DeepLink 回跳 |
| LLM 摘要耗时长导致 ANR | P1 | WorkManager 后台执行，UI 仅显示 loading |
| SAF URI 权限过期 | P2 | 过期时自动提示用户重新授权 |
| 通知数据过大导致 Room 卡顿 | P2 | 限制存储 ≤1000 条，超出时按时间淘汰最旧 |
