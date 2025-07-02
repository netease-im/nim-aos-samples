package com.netease.nim.samples.message.common

import android.content.Context
import com.netease.nim.samples.common.getV2NIMMessageJson
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.message.*
import com.netease.nimlib.sdk.v2.message.V2NIMMessage

/**
 * 全局消息监听器管理器
 * 用于管理消息监听器的生命周期和回调记录
 */
object GlobalMessageListenerManager {

    /**
     * 监听器实例
     */
    private var messageListener: V2NIMMessageListener? = null

    /**
     * 监听开始时间
     */
    private var listenStartTime: Long = 0

    /**
     * 回调历史记录
     */
    private val callbackHistory = mutableListOf<CallbackRecord>()

    /**
     * 统计信息
     */
    private val statistics = Statistics()

    /**
     * 回调记录数据类
     */
    data class CallbackRecord(
        val timestamp: Long,
        val eventType: String,
        val eventDetail: String,
        val eventData: String
    )

    /**
     * 统计信息数据类
     */
    data class Statistics(
        var totalCallbackCount: Int = 0,
        var receiveMessagesCount: Int = 0,
        var sendMessageCount: Int = 0,
        var revokeNotificationsCount: Int = 0,
        var deleteNotificationsCount: Int = 0,
        var readReceiptsCount: Int = 0
    )

    /**
     * 检查监听器是否已添加
     */
    fun isListenerAdded(): Boolean {
        return messageListener != null
    }

    /**
     * 添加监听器
     */
    fun addListener(context: Context): Boolean {
        if (messageListener != null) {
            return false // 已经添加了
        }

        messageListener = object : V2NIMMessageListener {
            override fun onReceiveMessages(messages: List<V2NIMMessage>) {
                recordCallback("onReceiveMessages[接收消息]", "收到${messages.size}条消息",
                    messages.joinToString("\n") { getV2NIMMessageJson(it) })
                statistics.receiveMessagesCount++
                statistics.totalCallbackCount++
            }

            override fun onReceiveP2PMessageReadReceipts(readReceipts: List<V2NIMP2PMessageReadReceipt>) {
                recordCallback("onReceiveP2PMessageReadReceipts[P2P已读回执]", "收到${readReceipts.size}条已读回执",
                    readReceipts.joinToString("\n") { "会话ID: ${it.conversationId}" })
                statistics.readReceiptsCount++
                statistics.totalCallbackCount++
            }

            override fun onReceiveTeamMessageReadReceipts(readReceipts: List<V2NIMTeamMessageReadReceipt>) {
                recordCallback("onReceiveTeamMessageReadReceipts[群已读回执]", "收到${readReceipts.size}条群已读回执",
                    readReceipts.joinToString("\n") { "消息ID: ${it.messageClientId}" })
                statistics.readReceiptsCount++
                statistics.totalCallbackCount++
            }

            override fun onMessageRevokeNotifications(revokeNotifications: List<V2NIMMessageRevokeNotification>) {
                recordCallback("onMessageRevokeNotifications[消息撤回]", "收到${revokeNotifications.size}条撤回通知",
                    revokeNotifications.joinToString("\n") { "消息ID: ${it.messageRefer?.messageClientId}" })
                statistics.revokeNotificationsCount++
                statistics.totalCallbackCount++
            }

            override fun onMessagePinNotification(pinNotification: V2NIMMessagePinNotification) {
                recordCallback("onMessagePinNotification[消息Pin状态]", "Pin状态变更",
                    "消息ID: ${pinNotification.pin?.messageRefer?.messageClientId}, Pin: ${pinNotification.pinState}")
                statistics.totalCallbackCount++
            }

            override fun onMessageQuickCommentNotification(quickCommentNotification: V2NIMMessageQuickCommentNotification) {
                recordCallback("onMessageQuickCommentNotification[快捷评论]", "评论状态变更",
                        "消息ID: ${quickCommentNotification.quickComment?.messageRefer?.messageClientId}")
                statistics.totalCallbackCount++
            }

            override fun onMessageDeletedNotifications(messageDeletedNotifications: List<V2NIMMessageDeletedNotification>) {
                recordCallback("onMessageDeletedNotifications[消息删除]", "收到${messageDeletedNotifications.size}条删除通知",
                    messageDeletedNotifications.joinToString("\n") { "消息ID: ${it.messageRefer?.messageClientId}" })
                statistics.deleteNotificationsCount++
                statistics.totalCallbackCount++
            }

            override fun onClearHistoryNotifications(clearHistoryNotifications: List<V2NIMClearHistoryNotification>) {
                recordCallback("onClearHistoryNotifications[清空历史]", "收到${clearHistoryNotifications.size}条清空历史通知",
                    clearHistoryNotifications.joinToString("\n") { "会话ID: ${it.conversationId}" })
                statistics.totalCallbackCount++
            }

            override fun onSendMessage(message: V2NIMMessage) {
                recordCallback("onSendMessage[发送消息]", "本端发送消息", getV2NIMMessageJson(message))
                statistics.sendMessageCount++
                statistics.totalCallbackCount++
            }

            override fun onReceiveMessagesModified(messages: List<V2NIMMessage>) {
                recordCallback("onReceiveMessagesModified[消息更新]", "收到${messages.size}条消息更新",
                    messages.joinToString("\n") { getV2NIMMessageJson(it) })
                statistics.totalCallbackCount++
            }
        }

        return try {
            NIMClient.getService(V2NIMMessageService::class.java).addMessageListener(messageListener!!)
            listenStartTime = System.currentTimeMillis()
            true
        } catch (e: Exception) {
            messageListener = null
            false
        }
    }

    /**
     * 移除监听器
     */
    fun removeListener(): Boolean {
        return if (messageListener != null) {
            try {
                NIMClient.getService(V2NIMMessageService::class.java).removeMessageListener(messageListener!!)
                messageListener = null
                listenStartTime = 0
                true
            } catch (e: Exception) {
                false
            }
        } else {
            false
        }
    }

    /**
     * 记录回调事件
     */
    private fun recordCallback(eventType: String, eventDetail: String, eventData: String) {
        val record = CallbackRecord(
            timestamp = System.currentTimeMillis(),
            eventType = eventType,
            eventDetail = eventDetail,
            eventData = eventData
        )
        
        callbackHistory.add(0, record) // 添加到开头，保持最新的在前面
        
        // 限制历史记录数量，避免内存过多占用
        if (callbackHistory.size > 100) {
            callbackHistory.removeAt(callbackHistory.size - 1)
        }
    }

    /**
     * 获取监听开始时间
     */
    fun getListenStartTime(): Long {
        return listenStartTime
    }

    /**
     * 获取统计信息
     */
    fun getStatistics(): Statistics {
        return statistics.copy()
    }

    /**
     * 获取回调历史记录
     */
    fun getCallbackHistory(): List<CallbackRecord> {
        return callbackHistory.toList()
    }

    /**
     * 获取最新的回调记录
     */
    fun getLatestCallback(): CallbackRecord? {
        return callbackHistory.firstOrNull()
    }

    /**
     * 清空历史记录
     */
    fun clearHistory() {
        callbackHistory.clear()
        statistics.totalCallbackCount = 0
        statistics.receiveMessagesCount = 0
        statistics.sendMessageCount = 0
        statistics.revokeNotificationsCount = 0
        statistics.deleteNotificationsCount = 0
        statistics.readReceiptsCount = 0
    }
}