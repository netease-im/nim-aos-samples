package com.netease.nim.samples.localconversation.common

import android.content.Context
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.V2NIMError
import com.netease.nimlib.sdk.v2.conversation.V2NIMLocalConversationListener
import com.netease.nimlib.sdk.v2.conversation.V2NIMLocalConversationService
import com.netease.nimlib.sdk.v2.conversation.model.V2NIMLocalConversation
import com.netease.nimlib.sdk.v2.conversation.params.V2NIMLocalConversationFilter
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 全局会话监听器管理器
 * 单例模式，确保全局只有一个监听器实例
 */
object GlobalConversationListenerManager {
    
    /**
     * 全局会话监听器实例
     */
    private var conversationListener: V2NIMLocalConversationListener? = null
    
    /**
     * 监听器是否已添加
     */
    private var isListenerAdded = false
    
    /**
     * 监听开始时间
     */
    private var listenStartTime = 0L
    
    /**
     * 历史记录列表（使用线程安全的List）
     */
    private val callbackHistory = CopyOnWriteArrayList<CallbackRecord>()
    
    /**
     * 回调统计
     */
    private var totalCallbackCount = 0
    private var syncEventCount = 0
    private var conversationChangeCount = 0
    private var unreadCountChangeCount = 0
    
    /**
     * UI更新回调列表
     */
    private val uiUpdateCallbacks = mutableListOf<() -> Unit>()

    /**
     * 回调记录数据类
     */
    data class CallbackRecord(
        val timestamp: Long,
        val eventType: String,
        val eventDetail: String,
        val eventData: String = ""
    )
    
    /**
     * 统计数据类
     */
    data class CallbackStatistics(
        val totalCallbackCount: Int,
        val syncEventCount: Int,
        val conversationChangeCount: Int,
        val unreadCountChangeCount: Int
    )

    /**
     * 添加全局监听器
     */
    @Synchronized
    fun addListener(context: Context): Boolean {
        if (isListenerAdded) {
            return false // 已经添加了
        }

        try {
            conversationListener = object : V2NIMLocalConversationListener {
                override fun onSyncStarted() {
                    addCallbackRecord("onSyncStarted", "同步开始", "会话数据同步开始")
                    syncEventCount++
                    notifyUIUpdate()
                }

                override fun onSyncFinished() {
                    addCallbackRecord("onSyncFinished", "同步完成", "会话数据同步完成")
                    syncEventCount++
                    notifyUIUpdate()
                }

                override fun onSyncFailed(error: V2NIMError?) {
                    addCallbackRecord("onSyncFailed", "同步失败", "错误信息: $error")
                    syncEventCount++
                    notifyUIUpdate()
                }

                override fun onConversationCreated(conversation: V2NIMLocalConversation?) {
                    val detail = "会话ID: ${conversation?.conversationId}"
                    addCallbackRecord("onConversationCreated", "会话创建", detail)
                    conversationChangeCount++
                    notifyUIUpdate()
                }

                override fun onConversationDeleted(conversationIds: List<String>?) {
                    val detail = "删除数量: ${conversationIds?.size}, IDs: ${conversationIds?.joinToString(", ")}"
                    addCallbackRecord("onConversationDeleted", "会话删除", detail)
                    conversationChangeCount++
                    notifyUIUpdate()
                }

                override fun onConversationChanged(conversationList: List<V2NIMLocalConversation>?) {
                    val detail = "变更数量: ${conversationList?.size}"
                    val data = conversationList?.joinToString("\n") { 
                        "ID: ${it.conversationId}, 未读: ${it.unreadCount}" 
                    } ?: ""
                    addCallbackRecord("onConversationChanged", "会话变更", detail, data)
                    conversationChangeCount++
                    notifyUIUpdate()
                }

                override fun onTotalUnreadCountChanged(unreadCount: Int) {
                    addCallbackRecord("onTotalUnreadCountChanged", "总未读数变更", "新未读数: $unreadCount")
                    unreadCountChangeCount++
                    notifyUIUpdate()
                }

                override fun onUnreadCountChangedByFilter(filter: V2NIMLocalConversationFilter?, unreadCount: Int) {
                    addCallbackRecord("onUnreadCountChangedByFilter", "过滤未读数变更", "未读数: $unreadCount, 过滤器: $filter")
                    unreadCountChangeCount++
                    notifyUIUpdate()
                }

                override fun onConversationReadTimeUpdated(conversationId: String?, readTime: Long) {
                    addCallbackRecord("onConversationReadTimeUpdated", "已读时间更新", "会话ID: $conversationId, 时间: $readTime")
                    notifyUIUpdate()
                }
            }

            conversationListener?.let {
                NIMClient.getService(V2NIMLocalConversationService::class.java).addConversationListener(it)
                isListenerAdded = true
                listenStartTime = System.currentTimeMillis()
                
                // 保存状态到SharedPreferences
                saveStateToPreferences(context)
                
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return false
    }

    /**
     * 移除全局监听器
     */
    @Synchronized
    fun removeListener(): Boolean {
        if (!isListenerAdded) {
            return false // 没有添加监听器
        }

        try {
            conversationListener?.let {
                NIMClient.getService(V2NIMLocalConversationService::class.java).removeConversationListener(it)
                isListenerAdded = false
                listenStartTime = 0L
                conversationListener = null
                
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return false
    }

    /**
     * 检查监听器是否已添加
     */
    fun isListenerAdded(): Boolean {
        return isListenerAdded
    }

    /**
     * 获取监听开始时间
     */
    fun getListenStartTime(): Long {
        return listenStartTime
    }

    /**
     * 获取最新的回调记录
     */
    fun getLatestCallback(): CallbackRecord? {
        return callbackHistory.firstOrNull()
    }

    /**
     * 获取所有回调历史
     */
    fun getCallbackHistory(): List<CallbackRecord> {
        return callbackHistory.toList()
    }

    /**
     * 获取统计信息
     */
    fun getStatistics(): CallbackStatistics {
        return CallbackStatistics(
            totalCallbackCount = totalCallbackCount,
            syncEventCount = syncEventCount,
            conversationChangeCount = conversationChangeCount,
            unreadCountChangeCount = unreadCountChangeCount
        )
    }

    /**
     * 清空历史记录
     */
    @Synchronized
    fun clearHistory() {
        callbackHistory.clear()
        totalCallbackCount = 0
        syncEventCount = 0
        conversationChangeCount = 0
        unreadCountChangeCount = 0
        notifyUIUpdate()
    }

    /**
     * 添加回调记录
     */
    private fun addCallbackRecord(eventType: String, eventDetail: String, eventData: String, extraData: String = "") {
        val record = CallbackRecord(
            timestamp = System.currentTimeMillis(),
            eventType = eventType,
            eventDetail = eventDetail,
            eventData = eventData + if (extraData.isNotEmpty()) "\n$extraData" else ""
        )
        
        callbackHistory.add(0, record) // 添加到列表开头
        
        // 限制历史记录数量，避免内存占用过多
        if (callbackHistory.size > 100) {
            callbackHistory.removeAt(callbackHistory.size - 1)
        }
        
        totalCallbackCount++
    }

    /**
     * 注册UI更新回调
     */
    fun registerUIUpdateCallback(callback: () -> Unit) {
        uiUpdateCallbacks.add(callback)
    }

    /**
     * 取消注册UI更新回调
     */
    fun unregisterUIUpdateCallback(callback: () -> Unit) {
        uiUpdateCallbacks.remove(callback)
    }

    /**
     * 通知UI更新
     */
    private fun notifyUIUpdate() {
        // 在主线程中通知UI更新
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            uiUpdateCallbacks.forEach { it.invoke() }
        }
    }

    /**
     * 保存状态到SharedPreferences
     */
    private fun saveStateToPreferences(context: Context) {
        try {
            val prefs = context.getSharedPreferences("global_conversation_listener", Context.MODE_PRIVATE)
            val editor = prefs.edit()
            
            editor.putBoolean("is_listener_added", isListenerAdded)
            editor.putLong("listen_start_time", listenStartTime)
            editor.putInt("total_callback_count", totalCallbackCount)
            editor.putInt("sync_event_count", syncEventCount)
            editor.putInt("conversation_change_count", conversationChangeCount)
            editor.putInt("unread_count_change_count", unreadCountChangeCount)
            
            editor.apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 从SharedPreferences加载状态
     */
    fun loadStateFromPreferences(context: Context) {
        try {
            val prefs = context.getSharedPreferences("global_conversation_listener", Context.MODE_PRIVATE)
            
            totalCallbackCount = prefs.getInt("total_callback_count", 0)
            syncEventCount = prefs.getInt("sync_event_count", 0)
            conversationChangeCount = prefs.getInt("conversation_change_count", 0)
            unreadCountChangeCount = prefs.getInt("unread_count_change_count", 0)
            
            // 注意：不恢复监听器状态，应用重启后需要重新添加监听器
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 应用退出时清理资源
     */
    fun onApplicationExit() {
        removeListener()
        callbackHistory.clear()
        uiUpdateCallbacks.clear()
    }
}