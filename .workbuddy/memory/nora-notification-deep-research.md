# 深度调研报告：Nora 通知聚合 & 文件上下文

> 调研时间：2026-04-25
> 调研人：WorkBuddy Agent
> 状态：✅ 结论明确，建议纳入 Phase 6

---

## 一、需求目标深度挖掘

### 1.1 这不只是"一个功能" — 是宪法维度的实现

回顾 Nora 宪法 2.2「能力四维模型」：

| 维度 | 宪法定义 | 本功能对应 |
|------|---------|-----------|
| **感知 (Sense)** | 通知监听、短信解析、相册索引、日历读取 | ✅ 通知监听 — 完全对齐 |
| **记忆 (Memory)** | 短期对话上下文、长期用户画像、加密存储 | ✅ 通知存储为长期记忆 |
| **行动 (Act)** | 通知回复、闹钟设置、文件整理（需确认） | ✅ 文件上下文注入对话 |
| **表达 (Express)** | 语气风格适配、文风模仿、摘要生成 | ✅ LLM 自动摘要 |

**结论**：这不是增强功能，是**宪法能力「感知维度」的直接落地**。没有通知感知，Nora 就只是"能聊天的本地 LLM"，而非"能感知环境的数字生命"。

### 1.2 隐私悖论的 Nora 解法

NotificationListenerService 本身是隐私争议焦点：
- Google Play 政策要求：申请通知访问权限的应用必须提供明确的隐私说明
- Android 15 新增 OTP 敏感通知保护（2025年）：即使有通知访问权限，也无法读取银行/验证码类通知
- 用户普遍对"读取所有通知"感到不安

**Nora 的隐私解法**：
```
传统方案：云端处理 → 用户数据上传 → 隐私风险
Nora 方案：本地处理 → 零网络请求 → 数据主权100%在用户手中
```

这是**竞品差异化核心**：没有第二个本地 AI 能做到"通知感知 + 离线摘要"。

### 1.3 用户价值分层

| 层次 | 价值 | 典型场景 |
|------|------|---------|
| **信息减负** | 减少通知焦虑 | "100条未读" → "Nora：今天重要的就这3条" |
| **记忆增强** | 不错过重要信息 | 开会时没看到的快递/银行通知，Nora 帮你记 |
| **上下文增强** | 对话更智能 | 问"Nora，帮我看看有什么待办"→ 基于通知生成 |
| **隐私主权** | 完全可控 | "你的数据，只有你见过" — 连 Nora 自己也不上传 |

---

## 二、技术调研结论

### 2.1 NotificationListenerService

**架构要点**：
```xml
<!-- AndroidManifest.xml -->
<service
    android:name=".notification.NoraNotificationService"
    android:permission="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE"
    android:exported="false">
    <intent-filter>
        <action android:name="android.service.notification.NotificationListenerService" />
    </intent-filter>
</service>
```

**关键发现**：
- 用户必须**主动**到系统设置授权（Settings → 应用 → 通知访问 → Nora），无法静默申请
- 监听范围 = 用户授权时选择的 App 范围（可配置）
- `onNotificationPosted(StatusBarNotification)` 回调在主线程，需及时释放
- Android 15 OTP 保护：验证码类通知带 `BRANDING_SHAKE` 或特定 package（如银行App）会被系统过滤，Nora 无法读取 → **这是好事**，符合隐私原则
- 防卸载：NotificationListenerService 在应用卸载前会被系统自动解除绑定

**核心 API**：
```kotlin
class NoraNotificationService : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        // sbn.packageName → 获取来源App
        // sbn.notification.extras → 获取标题/正文
        // sbn.postTime → 时间戳
        // 存入 Room
    }
    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // 可选：标记为已清除
    }
}
```

### 2.2 WorkManager PeriodicWork

**架构要点**：
- 最小周期：**15分钟**（Android 系统强制，最快间隔）
- 后台限制（Android 12+）：可能延迟到 15min + 弹性窗口
- 需要 `RECEIVE_BOOT_COMPLETED` 才能在重启后恢复
- Worker 运行在独立进程，主 App 卸载则 Worker 失效

**通知摘要 Worker 设计**：
```kotlin
class NotificationSummaryWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // 1. 读取未摘要通知（NotificationEntity.summary == null）
        // 2. 拼接 prompt
        // 3. 调用 LlmModule.inference()
        // 4. 解析 JSON → 更新 Room
        return Result.success()
    }
}

// 调度
val workRequest = PeriodicWorkRequestBuilder<NotificationSummaryWorker>(
    15, TimeUnit.MINUTES
).build()
WorkManager.getInstance(context).enqueueUniquePeriodicWork(
    "notification_summary",
    ExistingPeriodicWorkPolicy.KEEP,
    workRequest
)
```

### 2.3 SAF (Storage Access Framework)

**权限模型**：
```
用户主动选择文件 → 系统返回 content:// URI
     ↓
takePersistableUriPermission() → 持久化访问权限
     ↓
跨 App 重启保留（但用户可在设置中撤销）
```

**踩坑点**：
1. `ACTION_OPEN_DOCUMENT` 获取的 URI 默认无 `takePersistableUriPermission` 权限
2. 必须在 `onActivityResult` 中立即调用 `takePersistableUriPermission`
3. URI 权限可能被用户撤销（`ContentResolver.releasePersistableUriPermission`）
4. 需要在每次使用时检查 `checkUriPermission`

**文件上下文注入**：
```kotlin
val documentFile = DocumentFile.fromSingleUri(context, uri)
// 读取内容
val inputStream = context.contentResolver.openInputStream(uri)
val content = inputStream.bufferedReader().readText()
// 前512字符预览，存储到 Room
```

### 2.4 LLM 摘要集成

**现有资源复用**：
- 模型：`/data/local/tmp/llama/model.pte`（Qwen3-0.6B 量化）
- 推理引擎：现有 `LlmModule`（ExecuTorch）
- 已有流式输出能力

**摘要 Prompt 设计**：
```
你是一个信息整理助手。请归纳以下通知，每条用一句话概括，并给出分类（工作/社交/金融/快递/系统/其他）。

格式要求（JSON数组）：
[
  {"index": 0, "summary": "一句话概括", "category": "分类"},
  ...
]

通知列表：
1. [微信] 有人发来消息：[图片]
2. [韵达快递] 您的包裹已到达菜鸟驿站...
...

请仅输出JSON数组，不要其他内容。
```

**容错设计**：
- LLM 推理超时（60s）→ 标记为失败，跳过，Worker 继续处理下一批
- 内存不足（OOM）→ Worker 降级处理，减少 batch size（5→3）
- 模型未加载 → Worker 返回 `Result.retry()`，下次重试

---

## 三、风险 & 对策（更新）

| 风险 | 级别 | 对策 |
|------|------|------|
| 用户不授权通知访问 | P1 | 引导页清晰说明价值，暂不授权也可手动触发 |
| NotificationListener 被禁用 | P1 | 每次启动检查 `isNotificationListenerEnabled()`，引导用户重开 |
| Android 15 OTP 过滤 | P1 | 接受限制，不尝试绕过；OTP 本就不应被读取 |
| LLM 摘要耗时长导致 ANR | P1 | **Worker 后台执行**，UI 仅显示 loading 或最后摘要时间 |
| SAF URI 权限过期 | P2 | 每次使用前 `checkUriPermission`；过期则提示用户重新选择 |
| 通知数据过大（Room 性能） | P2 | **限制存储 ≤1000 条**；超出时按时间淘汰最旧；创建索引 |
| WorkManager 被系统优化杀死 | P2 | `setRequiresBatteryNotLow(false)`；提供手动触发入口 |
| INTERNET 权限意外引入 | P0 | **宪法审计第5项检查**（已在 Gate 机制中） |

---

## 四、与现有 Phase 的协同分析

### 4.1 Phase 1（数据持久化）→ 通知聚合

当前 Phase 1 Step 1 是「Application 级初始化 Room」。
通知聚合需要：
- Room Database 已初始化 ✅（Phase 1 Step 1）
- DataRepository 已可用 ✅（Phase 1 Step 2+）
- 通知存储在 Phase 1 基础上扩展 Entity/DAO

**结论**：Phase 6 强依赖 Phase 1，必须等 Room 基础就绪。

### 4.2 Phase 2（安全屋）→ 通知聚合

安全屋是 Nora 的主界面。通知聚合入口建议放在安全屋：
- 安全屋 Tab3：「感知」（通知入口）
- 通知 Badge：未读通知数（如果有）

**结论**：Phase 6 UI 入口需等 Phase 2 Navigation 框架就绪。

### 4.3 Phase 4（隐私仪表盘）→ 通知聚合

隐私仪表盘展示「权限使用记录」。
通知权限使用记录可以整合到 Phase 4 VaultScreen。

**结论**：Phase 6 与 Phase 4 有交叉，可以考虑在 Phase 4 中复用 Phase 6 的权限记录模块。

---

## 五、最终建议：纳入 Phase 6

### 5.1 定位

**Phase 6 — 感知层：通知聚合 & 文件上下文**

- 位置：Phase 5 之后
- 定位：宪法「感知 (Sense)」维度的正式落地
- 前置依赖：Phase 1 (Room 持久化)，Phase 2 (Navigation)，Phase 3 (对话 UI)

### 5.2 拆解（12 Steps + Gate）

```
Phase 6 — 感知层：通知聚合 & 文件上下文
目标：Nora 能感知手机通知、自动摘要、读取用户授权文件

[ ] Step 1: NotificationListenerService 实现（service declaration + XML）
[ ] Step 2: Room NotificationEntity + NotificationDao
[ ] Step 3: DataRepository 通知增删改查（同步 Phase 1 架构）
[ ] Step 4: 通知列表 UI — NotificationScreen（LazyColumn + 过滤Tab）
[ ] Step 5: 通知权限引导页（首次启动检测 + Settings DeepLink）
[ ] Step 6: 权限状态检测（isNotificationListenerEnabled）
[ ] Step 7: WorkManager PeriodicWork — NotificationSummaryWorker
[ ] Step 8: LLM 摘要 prompt + 解析 + Room 更新
[ ] Step 9: 摘要展示（NotificationCard 显示 LLM summary）
[ ] Step 10: SAF 文件选择器 + FileContextEntity
[ ] Step 11: 文件上下文注入对话（prompt 前缀拼接）
[ ] Step 12: Phase 6 Instrument 测试
    - NotificationListenerTest: 模拟通知 → 写入 Room
    - NotificationSummaryTest: Worker 执行 → 摘要生成
    - FileAccessTest: SAF → URI 持久化 → 内容读取
    - PermissionFlowTest: 权限引导 → 授权 → 通知读取
[ ] Gate: 编译 + Instrument 全绿 + 宪法审计（新增检查项：通知权限声明仅系统级）
```

### 5.3 新增宪法审计项

Phase 6 Gate 时，在原有 5 项审计基础上增加：

| # | 检查项 | 检查命令 | 期望 |
|---|--------|---------|------|
| 6 | NLS service declaration | `grep "NotificationListenerService" AndroidManifest.xml` | ≥1（必须有） |
| 7 | INTERNET 权限 | `grep "INTERNET" AndroidManifest.xml` | 0（严禁引入） |

---

## 六、总结

| 维度 | 结论 |
|------|------|
| **需求价值** | 宪法维度落地、数字生命核心能力、竞品差异化 |
| **技术可行性** | 100%（现有技术栈覆盖，无新依赖） |
| **隐私安全性** | 高（完全离线，INTERNET 禁令，Android 15 OTP 保护） |
| **与现有 Phase 关系** | 强依赖 Phase 1-2，必须串行推进 |
| **风险可控性** | P0 风险已识别并有对策（P1 WorkManager 后台执行） |
| **纳入建议** | ✅ **强烈建议纳入 Phase 6** |
