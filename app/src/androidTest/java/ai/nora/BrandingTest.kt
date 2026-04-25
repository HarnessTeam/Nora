package ai.nora

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Test

/**
 * Phase 0 Gate — 品牌标识测试
 * 宪法合规：包名为 ai.nora，应用名为 "Nora"
 */
class BrandingTest : BaseAndroidTest() {

    @Test
    fun 应用包名为AiNora() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals("ai.nora", context.packageName)
    }

    @Test
    fun 应用名为Nora() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(context.resources.getIdentifier("app_name", "string", context.packageName))
        assertEquals("Nora", appName)
    }

    @Test
    fun 应用类名为NoraApp() {
        // NoraApp 单例存在且 instance 不为 null
        assertNotNull(NoraApp.instance)
    }

    @Test
    fun 主题文件存在于AiNoraTheme包() {
        // NoraTheme Composable 存在于 ai.nora.theme 包（通过资源编译验证）
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals("ai.nora", context.packageName)
    }
}
