# 厚集成打包说明 (Bundled Flavor)

本目录用于存放内置模型文件，仅 **bundled** flavor 使用。

## 模型来源

从 [Qwen3 GGUF 模型下载页面](https://huggingface.co/Qwen/Qwen3-0.6B-GGUF) 下载量化版本。

## 配置步骤

### 1. 下载模型

访问 https://huggingface.co/Qwen/Qwen3-0.6B-GGUF ，下载 `q4_k_m` 量化版本。

### 2. 放置文件

将下载的文件重命名为 `model.pte`，并从 [Qwen3 tokenizer 页面](https://huggingface.co/Qwen/Qwen3-0.6B/tree/main) 下载 `tokenizer.json`：

```
app/src/main/assets/models/
├── model.pte          # 下载的量化模型，重命名
└── tokenizer.json     # 下载的分词器
```

### 3. 构建厚集成 APK

```bash
./gradlew assembleBundledDebug
```

## 注意事项

- **模型大小**：量化模型约 300-600 MB，请确保磁盘空间充足
- **zip 压缩**：AAPT2 会自动对 assets 目录进行 zip 压缩
- **SHA-256 校验**：首次提取后，App 会校验文件 hash，避免重复解压
- **slim flavor**：如需构建不含模型的 APK，使用 `./gradlew assembleSlimDebug`

## 文件说明

- `model.pte` — ExecuTorch 模型文件
- `tokenizer.json` — Qwen3 分词器配置
