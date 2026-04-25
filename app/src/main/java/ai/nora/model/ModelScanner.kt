package ai.nora.model

import ai.nora.BuildConfig
import android.content.Context
import android.util.Log
import java.io.File

/**
 * ModelScanner — 扫描设备上可用的模型文件
 *
 * 扫描路径优先级（bundled flavor 先看内置提取目录）：
 * 1. context.filesDir/models/       — bundled flavor 提取目录（最高优先）
 * 2. /data/local/tmp/llama/        — ADB 推送标准路径
 * 3. /sdcard/Download/llama/       — 下载目录备选
 *
 * slim flavor 不扫描 filesDir（无内置模型）。
 *
 * @param includeBundled true 时包含 filesDir/models/ 扫描路径（bundled flavor 使用）
 */
class ModelScanner(
    private val context: Context,
    private val includeBundled: Boolean = BuildConfig.MODEL_BUNDLED
) {

    /**
     * 模型来源类型 — 用于 UI 标签区分
     */
    enum class ModelSource {
        /** 从 assets 提取到 filesDir 的内置模型（bundled flavor） */
        BUNDLED,
        /** ADB push 到 /data/local/tmp/ 的外部模型 */
        ADB,
        /** 下载目录扫描到的模型 */
        DOWNLOAD
    }

    data class ModelFile(
        val name: String,
        val ptePath: File,
        val tokenizerPath: File,
        val pteSizeBytes: Long,
        val tokenizerSizeBytes: Long,
        /** 模型来源 — 用于 UI 显示标签 */
        val source: ModelSource = ModelSource.ADB
    ) {
        val displaySize: String get() = formatFileSize(pteSizeBytes)
    }

    private val scanDirs: List<Pair<File, ModelSource>> by lazy {
        buildList {
            // bundled flavor：优先扫描提取目录
            if (includeBundled) {
                add(File(context.filesDir, "models") to ModelSource.BUNDLED)
            }
            // 通用外部路径
            add(File("/data/local/tmp/llama") to ModelSource.ADB)
            add(File("/sdcard/Download/llama") to ModelSource.DOWNLOAD)
        }
    }

    fun scanModels(): List<ModelFile> {
        val results = mutableListOf<ModelFile>()
        for ((dir, source) in scanDirs) {
            try {
                if (!dir.exists() || !dir.isDirectory || !dir.canRead()) continue
                val pteFiles = dir.listFiles { f -> f.extension == "pte" } ?: continue
                for (pte in pteFiles) {
                    val tokenizer = findTokenizer(dir)
                    if (tokenizer != null) {
                        results.add(ModelFile(
                            name = pte.nameWithoutExtension,
                            ptePath = pte,
                            tokenizerPath = tokenizer,
                            pteSizeBytes = pte.length(),
                            tokenizerSizeBytes = tokenizer.length(),
                            source = source
                        ))
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error scanning ${dir.absolutePath}", e)
            }
        }
        return results.sortedByDescending { it.pteSizeBytes }
    }

    private fun findTokenizer(dir: File): File? {
        return listOf("tokenizer.json", "tokenizer.model", "tokenizer.bin")
            .map { File(dir, it) }
            .firstOrNull { it.exists() && it.canRead() }
    }

    companion object {
        private const val TAG = "ModelScanner"

        fun formatFileSize(bytes: Long): String = when {
            bytes >= 1_000_000_000 -> "%.1f GB".format(bytes / 1_000_000_000.0)
            bytes >= 1_000_000 -> "%.1f MB".format(bytes / 1_000_000.0)
            bytes >= 1_000 -> "%.1f KB".format(bytes / 1_000.0)
            else -> "$bytes B"
        }
    }
}
