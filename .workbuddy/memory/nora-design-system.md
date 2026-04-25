# Nora Design System v2 — Apple HIG 对齐版

> 基于 Apple Human Interface Guidelines + 豆包/ChatGPT 行业标杆
> 最后更新：2026-04-25
> 状态：**焊入渐进式开发流程**

---

## 一、设计哲学

### Apple HIG 核心原则（设计铁律）

| Apple HIG 原则 | Nora 落地方式 |
|----------------|--------------|
| ** Clarity** | 关键信息突出，无视觉噪音 |
| ** Depth** | 层次清晰，过渡自然 |
| ** Autonomy** | 用户控制，无侵入性打扰 |
| ** Responsiveness** | 即时反馈，无等待焦虑 |
| ** Consistency** | 模式一致，认知复用 |

### Nora 设计誓言

```
❌ 用户不需要知道模型是什么
❌ 用户不需要知道选择哪个模式
❌ 用户不需要手动配置任何东西
❌ 用户不需要看任何设置页面

✅ 用户只需要输入想说的话
✅ Nora 在后台默默准备好一切
✅ 结果直接呈现，无中间页
```

---

## 二、渐进式设计流程（焊入开发）

### 设计每个 Step 必须回答

每个 Phase Step 执行前，必须通过以下检查：

```
[ ] Apple 清晰原则：这个 UI 让用户一眼看到最重要的内容了吗？
[ ] Apple 层次原则：视觉层次清晰，关键操作突出？
[ ] Nora 誓言：用户需要知道/选择/配置什么吗？（应该是 NO）
[ ] 豆包策略：这个交互和豆包/ChatGPT 比够简洁吗？
[ ] 设计原则检查：参考下方组件规范
```

### 设计评审 Checklist（Gate 强制）

Phase Gate 时，自动化必须执行：

```
[ ] 1. Apple 清晰度：首屏能否用一句话描述？
[ ] 2. 认知负荷：用户首次使用需要学习什么？（应该是 0）
[ ] 3. 状态可见性：Nora 当前状态用户能感知吗？
[ ] 4. 反馈即时性：每个操作都有即时反馈吗？
[ ] 5. Nora 誓言：完全没有模型选择/设置页面？
```

---

## 三、颜色系统（#FF6B6B 橙为核心）

### 主色调

| 名称 | 色值 | 用途 |
|------|------|------|
| `NoraOrange` | `#FF6B6B` | 品牌色、用户消息、高亮、CTA |
| `NoraOrangeLight` | `#FF8A8A` | 悬停态、次要强调 |
| `NoraOrangeDark` | `#E85555` | 按压态、深度强调 |

### 背景色

| 名称 | 色值 | 用途 |
|------|------|------|
| `Background` | `#121212` | 全局背景（Apple 的纯黑策略） |
| `Surface` | `#1E1E1E` | 卡片、输入框、对话框 |
| `SurfaceElevated` | `#2A2A2A` | 浮层、BottomSheet |
| `Divider` | `#2C2C2C` | 分隔线、描边 |

### 文字色

| 名称 | 色值 | 用途 |
|------|------|------|
| `PrimaryText` | `#FFFFFF` | 主要文字 |
| `SecondaryText` | `#9E9E9E` | 次要文字、时间戳 |
| `TertiaryText` | `#666666` | 禁用态、占位符 |

### 状态色

| 名称 | 色值 | 用途 |
|------|------|------|
| `NoraReady` | `#4ADE80` | 模型就绪（Apple 绿） |
| `NoraThinking` | `#FACC15` | 思考中（Apple 黄） |
| `NoraError` | `#EF4444` | 错误态（Apple 红） |
| `NoraOffline` | `#6B7280` | 离线态 |

---

## 四、字体系统（Apple SF Pro 策略）

### 字体栈

```kotlin
// 优先使用系统字体（Apple SF Pro / Android Roboto）
FontFamily = systemDefault // 自动适配平台
CodeFontFamily = FontFamily.Monospace
```

### 字号规范

| 场景 | 字号 | 字重 | 行高 |
|------|------|------|------|
| 品牌 Logo | 28sp | Bold (700) | 1.2 |
| 大标题 | 24sp | SemiBold (600) | 1.3 |
| 卡片标题 | 18sp | Medium (500) | 1.4 |
| 正文 | 16sp | Regular (400) | 1.5 |
| 辅助文字 | 14sp | Regular (400) | 1.4 |
| 时间戳 | 12sp | Regular (400) | 1.3 |
| 代码 | 14sp | Regular (400) | 1.6 |

---

## 五、组件规范

### 1. 输入框（NoraInputBar）— Apple 简洁策略

**设计原则**：
- 大圆角胶囊（Corners: 24dp）— Apple 的一贯风格
- 底部固定，键盘弹出自动调整
- 无边框，纯背景色（Surface）
- 发送按钮：圆形 ArrowUp 图标，NoraOrange 填充

**状态**：

| 状态 | 视觉 |
|------|------|
| 空态 | 占位符"问 Nora 任何事..."，发送按钮禁用灰色 |
| 输入中 | 发送按钮启用 NoraOrange |
| 发送中 | 输入框禁用，发送按钮变为加载态 |
| 错误 | 输入框边框变红 |

**代码**：
```kotlin
@Composable
fun NoraInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
)
```

### 2. 消息气泡（MessageBubble）— NoraOrange 用户优先

**用户消息**：
- 背景：`NoraOrange` (#FF6B6B)
- 文字：`#FFFFFF`
- 圆角：16dp（左侧下圆角）
- 最大宽度：屏幕 80%

**助手消息**：
- 背景：`Surface` (#1E1E1E)
- 文字：`PrimaryText` (#FFFFFF)
- 边框：1dp `Divider` (#2C2C2C)
- 圆角：16dp（右侧下圆角）
- 左侧：Nora 头像 + 呼吸光点动画

**思考状态**：
- 文案：「Nora 正在思考...」（替代三个点）
- 样式：SecondaryText + 渐变出现动画

### 3. 欢迎区块（WelcomeSection）— 豆包策略

**设计原则**：
- Logo + 欢迎语居中
- 快捷功能卡片（3张）：读文件 / 看通知 / 写代码
- 无模型选择下拉框

**组件结构**：
```kotlin
@Composable
fun WelcomeSection(
    onQuickAction: (QuickAction) -> Unit,
    modifier: Modifier = Modifier
)

enum class QuickAction {
    READ_FILE,      // 📄 读文件
    VIEW_NOTIFY,    // 🔔 看通知
    WRITE_CODE      // 💻 写代码
}
```

### 4. 顶部栏（NoraTopBar）— Apple 导航策略

**设计原则**：
- 左：Nora Logo（图标 + 文字）
- 中：当前对话标题（可点击展开对话列表）
- 右：状态指示器（就绪/思考中/异常）

**高度**：56dp（Apple 标准）

### 5. 状态指示器（ModelStatusIndicator）

**就绪态**（默认）：
- 绿色圆点 + 「Nora 就绪」
- 静默，不打扰

**思考态**：
- 黄色脉冲 + 「Nora 思考中...」
- 实时响应

**异常态**：
- 红色圆点 + 「模型加载失败」
- 提供一键修复按钮

---

## 六、交互规范

### 动效原则（Apple 层次感）

| 动效 | 时长 | 曲线 | 用途 |
|------|------|------|------|
| 消息出现 | 250ms | easeOut | 聊天气泡 |
| 页面切换 | 300ms | easeInOut | 导航过渡 |
| 按钮反馈 | 100ms | easeIn | 按压态 |
| 加载脉冲 | 1500ms | linear (repeat) | 呼吸光点 |
| 骨架屏 | 1000ms | linear (repeat) | 内容加载 |

### 手势支持

- **下滑**：刷新内容
- **长按消息**：复制/删除菜单
- **左滑**：快速删除（带确认）
- **双击头像**：快速重试

### 错误处理

| 场景 | 处理方式 |
|------|---------|
| 模型加载失败 | 显示红色状态，点击一键重试 |
| 消息发送失败 | 输入框保留内容，显示重发按钮 |
| 网络相关 | 不存在（Nora 完全离线） |

---

## 七、渐进式开发设计检查点

### Phase 0（品牌化）设计验收

- [ ] 无紫色残留（Material 默认色）
- [ ] 全局暗色背景（#121212）
- [ ] NoraOrange (#FF6B6B) 存在于关键位置
- [ ] 字体无衬线，清晰可读

### Phase 1（持久化）设计验收

- [ ] 输入框大圆角胶囊风格
- [ ] 消息气泡有正确圆角
- [ ] 状态指示器可见

### Phase 2（安全屋）设计验收

- [ ] 呼吸光点动画流畅
- [ ] 快捷功能卡片简洁
- [ ] 无设置/模型选择入口

### Phase 3（对话 UI）设计验收

- [ ] Apple 清晰：首屏一眼看出这是聊天 App
- [ ] 消息气泡层次清晰
- [ ] 思考状态文案友好
- [ ] 输入框 Apple 风格大圆角

### Phase 4（隐私）设计验收

- [ ] 文字描述温暖可信赖
- [ ] 开关切换即时反馈
- [ ] 数据可视化（Apple 图表风格）

### Phase 5（技能树）设计验收

- [ ] 技能节点状态清晰
- [ ] 动画流畅有层次
- [ ] 交互无认知负荷

### Phase 6（感知）设计验收

- [ ] 通知列表 Apple 风格
- [ ] 权限引导简洁明了
- [ ] 文件选择无侵入

---

## 八、禁止模式（设计红线）

```
🚫 禁止：模型选择下拉框
🚫 禁止：专业术语展示（如"Qwen3-0.6B"）
🚫 禁止：设置页面入口
🚫 禁止：深色/浅色主题切换
🚫 禁止：复杂的权限说明弹窗
🚫 禁止：首屏任何配置引导
🚫 禁止：通知权限强制弹窗（静默检测，用户主动触发引导）
```

---

## 九、设计自动化检查

每次自动化执行时，自动检查：

```bash
# 1. 检查无模型选择 UI
grep -r "model.*select\|ModelSelector" app/src/main/

# 2. 检查无设置入口
grep -r "settings.*icon\|SettingsScreen" app/src/main/

# 3. 检查颜色规范
grep -r "0xFF6750A4\|Material.*Purple" app/src/main/

# 4. 检查圆角规范（输入框）
grep -r "cornerRadius.*24" app/src/main/

# 5. Apple 清晰度检查：首屏组件
grep -r "WelcomeSection\|NoraTopBar\|NoraInputBar" app/src/main/
```

---

## 十、参考标杆

### Apple HIG

- SF Pro 字体策略
- 56dp 导航栏
- 大圆角输入框
- 状态指示器设计

### 豆包

- 首屏快捷功能卡片
- 简洁的欢迎语
- 无模型选择的对话流

### ChatGPT

- 深色极简界面
- 大的输入框
- 流式输出动画

---

*本文档与 nora-phase-tracker.md 同步更新，每次 Phase Gate 时自动执行设计合规检查。*
