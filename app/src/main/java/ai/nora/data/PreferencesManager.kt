package ai.nora.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nora_preferences")

/**
 * NoraApp 级偏好设置管理器
 * 持久化模型加载状态，支持跨 Session 恢复导航默认页
 */
class PreferencesManager(private val context: Context) {

    companion object {
        private val KEY_MODEL_LOADED = booleanPreferencesKey("model_loaded")
        private val KEY_SELECTED_MODEL_PATH = stringPreferencesKey("selected_model_path")
    }

    /**
     * 模型是否已加载过（跨 Session 持久化）
     * 用于启动时决定默认导航页：true → Chat, false → Setup
     */
    val modelLoaded: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_MODEL_LOADED] ?: false
    }

    /**
     * 上次选择的模型路径（用于自动加载）
     */
    val selectedModelPath: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_SELECTED_MODEL_PATH]
    }

    /** 标记模型已加载（SetupScreen 回调时写入） */
    suspend fun setModelLoaded(path: String? = null) {
        context.dataStore.edit { prefs ->
            prefs[KEY_MODEL_LOADED] = true
            path?.let { prefs[KEY_SELECTED_MODEL_PATH] = it }
        }
    }

    /** 清除模型加载状态（用户卸载模型时） */
    suspend fun clearModelLoaded() {
        context.dataStore.edit { prefs ->
            prefs[KEY_MODEL_LOADED] = false
            prefs.remove(KEY_SELECTED_MODEL_PATH)
        }
    }
}
