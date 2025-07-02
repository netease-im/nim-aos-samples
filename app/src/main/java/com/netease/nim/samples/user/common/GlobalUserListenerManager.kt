package com.netease.nim.samples.user.common

import android.content.Context
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.user.V2NIMUser
import com.netease.nimlib.sdk.v2.user.V2NIMUserListener
import com.netease.nimlib.sdk.v2.user.V2NIMUserService
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 全局用户监听器管理器
 * 管理全局唯一的用户监听器，收集和存储所有回调事件
 */
object GlobalUserListenerManager {

    /**
     * 监听器实例
     */
    private var userListener: V2NIMUserListener? = null

    /**
     * 监听器是否已添加
     */
    private var isListenerAdded = false

    /**
     * 监听开始时间
     */
    private var listenStartTime = 0L

    /**
     * 回调历史记录
     */
    private val callbackHistory = CopyOnWriteArrayList<CallbackRecord>()

    /**
     * 统计信息
     */
    private var statistics = CallbackStatistics()

    /**
     * 时间格式化器
     */
    private val dateTimeFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

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
    data class CallbackStatistics(
        var totalCallbackCount: Int = 0,
        var userProfileChangeCount: Int = 0,
        var blockListAddedCount: Int = 0,
        var blockListRemovedCount: Int = 0
    )

    /**
     * 添加用户监听器
     */
    fun addListener(context: Context): Boolean {
        return try {
            if (isListenerAdded) {
                return false // 已经添加过了
            }

            userListener = createUserListener()
            NIMClient.getService(V2NIMUserService::class.java).addUserListener(userListener!!)
            
            isListenerAdded = true
            listenStartTime = System.currentTimeMillis()
            
            // 记录添加监听器事件
            addCallbackRecord(
                "LISTENER_ADDED",
                "用户监听器添加成功",
                "监听器已成功添加到用户服务中，开始监听用户资料相关事件"
            )
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 创建用户监听器
     */
    private fun createUserListener(): V2NIMUserListener {
        return object : V2NIMUserListener {
            override fun onUserProfileChanged(users: List<V2NIMUser>) {
                handleUserProfileChanged(users)
            }

            override fun onBlockListAdded(user: V2NIMUser) {
                handleBlockListAdded(user)
            }

            override fun onBlockListRemoved(accountId: String) {
                handleBlockListRemoved(accountId)
            }
        }
    }

    /**
     * 处理用户资料变更事件
     */
    private fun handleUserProfileChanged(users: List<V2NIMUser>) {
        statistics.totalCallbackCount++
        statistics.userProfileChangeCount++

        val userInfo = users.joinToString(", ") { user ->
            "${user.accountId}(${user.name ?: "null"})"
        }

        val eventDetail = "onUserProfileChanged[用户资料变更通知]"
        val eventData = "变更用户数量: ${users.size}\n变更用户: $userInfo\n" +
                "时间: ${dateTimeFormatter.format(Date())}"

        addCallbackRecord("USER_PROFILE_CHANGED", eventDetail, eventData)
    }

    /**
     * 处理黑名单添加事件
     */
    private fun handleBlockListAdded(user: V2NIMUser) {
        statistics.totalCallbackCount++
        statistics.blockListAddedCount++

        val eventDetail = "onBlockListAdded[用户添加到黑名单]"
        val eventData = "账号ID: ${user.accountId}\n" +
                "用户昵称: ${user.name ?: "null"}\n" +
                "时间: ${dateTimeFormatter.format(Date())}"

        addCallbackRecord("BLOCK_LIST_ADDED", eventDetail, eventData)
    }

    /**
     * 处理黑名单移除事件
     */
    private fun handleBlockListRemoved(accountId: String) {
        statistics.totalCallbackCount++
        statistics.blockListRemovedCount++

        val eventDetail = "onBlockListRemoved[用户从黑名单移除]"
        val eventData = "账号ID: $accountId\n" +
                "时间: ${dateTimeFormatter.format(Date())}"

        addCallbackRecord("BLOCK_LIST_REMOVED", eventDetail, eventData)
    }

    /**
     * 添加回调记录
     */
    private fun addCallbackRecord(eventType: String, eventDetail: String, eventData: String) {
        val record = CallbackRecord(
            timestamp = System.currentTimeMillis(),
            eventType = eventType,
            eventDetail = eventDetail,
            eventData = eventData
        )

        // 添加到历史记录开头
        callbackHistory.add(0, record)

        // 限制历史记录数量，避免内存泄漏
        if (callbackHistory.size > 100) {
            callbackHistory.removeAt(callbackHistory.size - 1)
        }
    }

    /**
     * 移除用户监听器
     */
    fun removeListener(): Boolean {
        return try {
            userListener?.let { listener ->
                NIMClient.getService(V2NIMUserService::class.java).removeUserListener(listener)
                userListener = null
                isListenerAdded = false
                
                addCallbackRecord(
                    "LISTENER_REMOVED",
                    "用户监听器移除成功",
                    "监听器已从用户服务中移除，停止监听用户资料相关事件"
                )
                
                true
            } ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
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
     * 获取统计信息
     */
    fun getStatistics(): CallbackStatistics {
        return statistics.copy()
    }

    /**
     * 清空历史记录
     */
    fun clearHistory() {
        callbackHistory.clear()
        statistics = CallbackStatistics()
    }

    /**
     * 重置监听器状态（通常在应用重启时调用）
     */
    fun reset() {
        userListener = null
        isListenerAdded = false
        listenStartTime = 0L
        callbackHistory.clear()
        statistics = CallbackStatistics()
    }
}