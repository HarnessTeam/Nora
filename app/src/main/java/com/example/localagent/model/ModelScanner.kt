package ai.nora.model

import android.content.Context
import android.util.Log
import java.io.File

class ModelScanner(private val context: Context) {

    data class ModelFile(
        val name: String,
        val ptePath: File,
        val tokenizerPath: File,
        val pteSizeBytes: Long,
        val tokenizerSizeBytes: Long
    ) {
        val displaySize: String get() = formatFileSize(pteSizeBytes)
    }

    private val scanDirs: List<File> = listOf(
        File("/data/local/tmp/llama"),
        File("/sdcard/Download/llama"),
        File(context.filesDir, "models")
    )

    fun scanModels(): List<ModelFile> {
        val results = mutableListOf<ModelFile>()
        for (dir in scanDirs) {
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
                            tokenizerSizeBytes = tokenizer.length()
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
