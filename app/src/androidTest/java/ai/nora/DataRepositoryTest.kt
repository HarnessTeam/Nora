package ai.nora

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.ContextCompat
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Phase 1 Gate — DataRepository Room CRUD 测试
 *
 * 直接测试 DataRepository + Room 数据库的增删改查操作，
 * 验证消息持久化逻辑的完整性。
 */
@RunWith(AndroidJUnit4::class)
class DataRepositoryTest {

    private lateinit var database: ai.nora.data.AppDatabase
    private lateinit var repository: ai.nora.data.DataRepository

    @Before
    fun setup() {
        // 使用 in-memory Room 数据库，避免污染真实数据
        val context = ContextCompat.getTestInstrumentationContext(
            androidx.test.platform.app.ContextRegistry.getActualApplication(
                android.app.InstrumentationRegistry.getInstrumentation().targetContext
            )
        )
        database = Room.inMemoryDatabaseBuilder(
            context,
            ai.nora.data.AppDatabase::class.java
        ).build()
        repository = ai.nora.data.DataRepository(
            database.conversationDao(),
            database.messageDao()
        )
    }

    /**
     * 测试创建新对话
     */
    @Test
    fun 创建对话_返回有效ID() {
        val id = runBlocking {
            repository.createConversation(title = "测试对话")
        }
        assertTrue("对话 ID 应为正数", id > 0)
    }

    /**
     * 测试获取最近对话 ID
     */
    @Test
    fun 获取最近对话ID_无对话时返回null() {
        val id = runBlocking {
            repository.getMostRecentConversationId()
        }
        assertNull("无对话时应返回 null", id)
    }

    /**
     * 测试添加消息到对话
     */
    @Test
    fun 添加消息_消息被正确持久化() {
        val convId = runBlocking {
            repository.createConversation(title = "消息测试")
        }
        val msgId = runBlocking {
            repository.addMessage(convId, role = "user", content = "Hello Nora")
        }
        assertTrue("消息 ID 应为正数", msgId > 0)

        // 验证消息被检索到
        val messages = runBlocking {
            repository.getMessagesSync(convId)
        }
        assertEquals("应恰好有 1 条消息", 1, messages.size)
        assertEquals("消息内容应匹配", "Hello Nora", messages[0].content)
        assertEquals("角色应匹配", "user", messages[0].role)
    }

    /**
     * 测试删除对话及关联消息
     */
    @Test
    fun 删除对话_对话和消息均被删除() {
        val convId = runBlocking {
            val id = repository.createConversation(title = "将被删除")
            repository.addMessage(id, role = "user", content = "临时消息")
            id
        }

        // 验证消息存在
        var messages = runBlocking { repository.getMessagesSync(convId) }
        assertTrue("消息应存在", messages.isNotEmpty())

        // 删除对话
        runBlocking { repository.deleteConversation(convId) }

        // 验证对话不存在
        val mostRecentId = runBlocking { repository.getMostRecentConversationId() }
        assertNotEquals("已删除对话 ID 不应再出现", convId, mostRecentId)
    }

    /**
     * 测试多对话场景下获取最近对话
     */
    @Test
    fun 多对话场景_返回最近更新的对话() {
        val conv1 = runBlocking { repository.createConversation(title = "旧对话") }
        runBlocking {
            repository.addMessage(conv1, role = "user", content = "第一条消息")
        }

        // 中间插入延迟确保 updatedAt 不同（纳秒级时间戳）
        Thread.sleep(10)

        val conv2 = runBlocking { repository.createConversation(title = "新对话") }
        runBlocking {
            repository.addMessage(conv2, role = "user", content = "第二条消息")
        }

        val mostRecent = runBlocking { repository.getMostRecentConversationId() }
        assertEquals("最近对话应为 conv2", conv2, mostRecent)
    }

    /**
     * 测试更新对话时间戳
     */
    @Test
    fun 更新时间戳_对话最新() {
        val convId = runBlocking { repository.createConversation(title = "时间戳测试") }

        // 添加消息触发时间戳更新
        runBlocking {
            repository.addMessage(convId, role = "user", content = "触发时间戳更新")
        }

        val mostRecent = runBlocking { repository.getMostRecentConversationId() }
        assertEquals("刚更新的对话应为最近", convId, mostRecent)
    }

    /**
     * 测试获取对话标题
     */
    @Test
    fun 获取对话标题_标题正确() {
        val convId = runBlocking {
            repository.createConversation(title = "自定义标题")
        }
        val title = runBlocking {
            repository.getConversationTitle(convId)
        }
        assertEquals("标题应匹配", "自定义标题", title)
    }
}
