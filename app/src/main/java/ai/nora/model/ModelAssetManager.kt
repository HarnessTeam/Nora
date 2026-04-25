package ai.nora.model

import ai.nora.BuildConfig
import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

/**
 * ModelAssetManager — 内置模型（bundled flavor）提取管理器
 *
 * 职责：
 * 1. 将 assets/models/ 下的 model.pte + tokenizer 提取到 filesDir/models/
 * 2. 基于 SHA-256 校验决定是否重新提取（避免每次启动重复解压）
 * 3. 提供统一的模型路径查询接口
 *
 * 仅在 MODEL_BUNDLED=true 时激活。
 * slim flavor 调用任何方法均为 no-op（返回 null）。
 *
 * assets 目录结构：
 *   assets/models/
 *   ├── model.pte          # 量化模型文件（zip 压缩已由 AAPT2 处理）
 *   └── tokenizer.json      # 分词器
 */
object ModelAssetManager {

    private const val TAG = "ModelAssetManager"
    private const val MODELS_DIR = "models"
    private const val ASSET_MODEL = "models/model.pte"
    private const val ASSET_TOKENIZER = "models/tokenizer.json"

    /**
     * 提取进度回调
     */
    interface ExtractionProgress {
        fun onProgress(current: Int, total: Int, filename: String)
        fun onComplete(extractedModelPath: String, extractedTokenizerPath: String)
        fun onError(message: String, throwable: Throwable? = null)
    }

    /**
     * 检查是否已有提取好的内置模型
     * @return 已提取的模型绝对路径；如果不存在或未内置则返回 null
     */
    fun getExtractedModelPath(context: Context): String? {
        if (!BuildConfig.MODEL_BUNDLED) return null
        val modelFile = File(getModelsDir(context), "model.pte")
        return if (modelFile.exists()) modelFile.absolutePath else null
    }

    /**
     * 检查是否已有提取好的分词器
     * @return 已提取的分词器绝对路径；如果不存在或未内置则返回 null
     */
    fun getExtractedTokenizerPath(context: Context): String? {
        if (!BuildConfig.MODEL_BUNDLED) return null
        val tokFile = File(getModelsDir(context), "tokenizer.json")
        return if (tokFile.exists()) tokFile.absolutePath else null
    }

    /**
     * 检查 assets 中内置模型是否比已提取的版本更新
     * 通过对比 SHA-256 哈希判断
     * @return true 需要重新提取
     */
    fun needsReExtraction(context: Context): Boolean {
        if (!BuildConfig.MODEL_BUNDLED) return false
        val extractedModel = File(getModelsDir(context), "model.pte")
        if (!extractedModel.exists()) return true

        // 比较 asset hash 和已提取文件 hash
        return try {
            val assetHash = computeAssetHash(context, ASSET_MODEL)
            val extractedHash = computeFileHash(extractedModel)
            assetHash != extractedHash
        } catch (e: Exception) {
            Log.w(TAG, "Hash comparison failed, re-extracting", e)
            true
        }
    }

    /**
     * 执行模型提取（协程，需在 IO dispatcher 调用）
     * - 仅 MODEL_BUNDLED=true 时生效
     * - 幂等：文件已存在且 hash 匹配时跳过
     * - 提取到 filesDir/models/ 目录
     *
     * @param context Application context
     * @param progress 可选进度回调
     * @return 提取后的模型路径，失败返回 null
     */
    suspend fun extractBundledModel(
        context: Context,
        progress: ExtractionProgress? = null
    ): Pair<String, String>? = withContext(Dispatchers.IO) {
        if (!BuildConfig.MODEL_BUNDLED) {
            Log.w(TAG, "extractBundledModel called on slim flavor — no-op")
            return@withContext null
        }

        val modelsDir = getModelsDir(context)
        if (!modelsDir.exists() && !modelsDir.mkdirs()) {
            val msg = "Failed to create models directory: ${modelsDir.absolutePath}"
            Log.e(TAG, msg)
            progress?.onError(msg)
            return@withContext null
        }

        val destModel = File(modelsDir, "model.pte")
        val destTokenizer = File(modelsDir, "tokenizer.json")

        // 幂等检查
        if (destModel.exists() && destTokenizer.exists() && !needsReExtraction(context)) {
            Log.i(TAG, "Bundled model already extracted: ${destModel.absolutePath}")
            return@withContext Pair(destModel.absolutePath, destTokenizer.absolutePath)
        }

        // 提取 model.pte
        try {
            progress?.onProgress(0, 2, "model.pte")
            extractSingleAsset(context, ASSET_MODEL, destModel)
            progress?.onProgress(1, 2, "model.pte")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to extract model.pte from assets", e)
            progress?.onError("Failed to extract model: ${e.message}", e)
            return@withContext null
        }

        // 提取 tokenizer.json
        try {
            progress?.onProgress(1, 2, "tokenizer.json")
            extractSingleAsset(context, ASSET_TOKENIZER, destTokenizer)
            progress?.onProgress(2, 2, "tokenizer.json")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to extract tokenizer.json from assets", e)
            // tokenizer 缺失不阻断，但记录
            Log.w(TAG, "Tokenizer not found in assets, model extraction partial")
        }

        Log.i(TAG, "Bundled model extracted: ${destModel.absolutePath}")
        progress?.onComplete(destModel.absolutePath, destTokenizer.absolutePath)
        return@withContext Pair(destModel.absolutePath, destTokenizer.absolutePath)
    }

    /**
     * 获取模型提取目标目录
     */
    private fun getModelsDir(context: Context): File {
        return File(context.filesDir, MODELS_DIR)
    }

    /**
     * 从 assets 提取单个文件
     * @throws Exception 如果 asset 不存在或 IO 失败
     */
    private fun extractSingleAsset(context: Context, assetPath: String, destFile: File) {
        context.assets.open(assetPath).use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output, bufferSize = 8192)
            }
        }
        Log.d(TAG, "Extracted asset: $assetPath -> ${destFile.absolutePath}")
    }

    /**
     * 计算 assets 中指定文件的 SHA-256 哈希（前 1MB 用于快速校验）
     */
    private fun computeAssetHash(context: Context, assetPath: String): String? {
        return try {
            context.assets.open(assetPath).use { input ->
                val digest = MessageDigest.getInstance("SHA-256")
                val buffer = ByteArray(1024 * 1024) // 1MB sample
                var read: Int
                var totalRead = 0L
                while (input.read(buffer).also { read = it } != -1 && totalRead < 1024 * 1024) {
                    digest.update(buffer, 0, read)
                    totalRead += read
                }
                digest.digest().joinToString("") { "%02x".format(it) }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to compute asset hash for $assetPath", e)
            null
        }
    }

    /**
     * 计算文件的 SHA-256 哈希（前 1MB）
     */
    private fun computeFileHash(file: File): String? {
        return try {
            file.inputStream().use { input ->
                val digest = MessageDigest.getInstance("SHA-256")
                val buffer = ByteArray(1024 * 1024)
                var read: Int
                var totalRead = 0L
                while (input.read(buffer).also { read = it } != -1 && totalRead < 1024 * 1024) {
                    digest.update(buffer, 0, read)
                    totalRead += read
                }
                digest.digest().joinToString("") { "%02x".format(it) }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to compute file hash for ${file.absolutePath}", e)
            null
        }
    }
}
