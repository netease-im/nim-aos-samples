package com.netease.nim.samples.user.common

import android.content.Context
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.friend.V2NIMFriend
import com.netease.nimlib.sdk.v2.friend.V2NIMFriendAddApplication
import com.netease.nimlib.sdk.v2.friend.V2NIMFriendListener
import com.netease.nimlib.sdk.v2.friend.V2NIMFriendService
import com.netease.nimlib.sdk.v2.friend.enums.V2NIMFriendDeletionType
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 全局用户监听器管理器
 * 管理全局唯一的用户监听器，收集和存储所有回调事件
 */
object GlobalFriendListenerManager {

    /**
     * 监听器实例
     */
    private var friendListener: V2NIMFriendListener? = null

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
        var friendProfileChangeCount: Int = 0,
        var friendAddedCount: Int = 0,
        var friendRemovedCount: Int = 0
    )

    /**
     * 添加用户监听器
     */
    fun addListener(context: Context): Boolean {
        return try {
            if (isListenerAdded) {
                return false // 已经添加过了
            }

            friendListener = createFriendListener()
            NIMClient.getService(V2NIMFriendService::class.java).addFriendListener(friendListener!!)
            
            isListenerAdded = true
            listenStartTime = System.currentTimeMillis()
            
            // 记录添加监听器事件
            addCallbackRecord(
                "LISTENER_ADDED",
                "好友监听器添加成功",
                "监听器已成功添加到好友服务中，开始监听好友相关事件"
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
    private fun createFriendListener(): V2NIMFriendListener {
        return object : V2NIMFriendListener {

            override fun onFriendAddApplication(applicationInfo: V2NIMFriendAddApplication?) {
               handleFriendAddApplication(applicationInfo)
            }

            override fun onFriendAdded(friendInfo: V2NIMFriend?) {
                handleFriendAdded(friendInfo)
            }

            override fun onFriendAddRejected(rejectionInfo: V2NIMFriendAddApplication?) {
                handleFriendAddRejected(rejectionInfo)
            }

            override fun onFriendInfoChanged(friendInfo: V2NIMFriend?) {
                handleFriendInfoChanged(friendInfo)
            }

            override fun onFriendDeleted(
                accountId: String?,
                deletionType: V2NIMFriendDeletionType?
            ) {
                handleFriendDeleted(accountId)
            }
        }
    }

    /**
     * 处理好友资料变更事件
     */
    private fun handleFriendInfoChanged(friendInfo: V2NIMFriend?) {
        if (friendInfo == null){
            return
        }
        statistics.totalCallbackCount++
        statistics.friendProfileChangeCount++

        val userInfo = "${friendInfo.accountId}(${friendInfo.alias ?: "null"})"


        val eventDetail = "onFriendInfoChanged[好友资料变更通知]"
        val eventData = "变更好友: $userInfo\n" +
                "时间: ${dateTimeFormatter.format(Date())}"

        addCallbackRecord("FRIEND_INFO_CHANGED", eventDetail, eventData)
    }

    /**
     * 处理添加好友事件
     */
    private fun handleFriendAdded(friendInfo: V2NIMFriend?) {
        if (friendInfo == null){
            return
        }
        statistics.totalCallbackCount++
        statistics.friendAddedCount++

        val eventDetail = "onFriendAdded[用户添加到好友]"
        val eventData = "好友账号ID: ${friendInfo.accountId}\n" +
                "好友昵称: ${friendInfo.alias ?: "null"}\n" +
                "时间: ${dateTimeFormatter.format(Date())}"

        addCallbackRecord("FRIEND_ADDED", eventDetail, eventData)
    }

    /**
     * 处理好友移除事件
     */
    private fun handleFriendDeleted(accountId: String?) {
        if (accountId == null){
            return
        }
        statistics.totalCallbackCount++
        statistics.friendRemovedCount++

        val eventDetail = "onFriendDeleted[用户从好友中移除]"
        val eventData = "账号ID: $accountId\n" +
                "时间: ${dateTimeFormatter.format(Date())}"

        addCallbackRecord("FRIEND_DELETE", eventDetail, eventData)
    }

    /**
     * 处理好友申请事件
     */
    private fun handleFriendAddApplication(applicationInfo: V2NIMFriendAddApplication?) {
        if (applicationInfo == null){
            return
        }
        statistics.totalCallbackCount++
        statistics.friendRemovedCount++

        val eventDetail = "onFriendAddApplication[添加好友申请]"
        val eventData = "申请者账号: ${applicationInfo.applicantAccountId}accountId\n" +
                "申请留言: ${applicationInfo.postscript}\n" +
                "被申请者账号: ${applicationInfo.recipientAccountId}\n" +
                "操作者账号: ${applicationInfo.operatorAccountId}\n" +
                "时间: ${dateTimeFormatter.format(Date())}"

        addCallbackRecord("FRIEND_ADD_APPLICATION", eventDetail, eventData)
    }

    /**
     * 处理好友申请被拒事件
     */
    private fun handleFriendAddRejected(applicationInfo: V2NIMFriendAddApplication?) {
        if (applicationInfo == null){
            return
        }
        statistics.totalCallbackCount++
        statistics.friendRemovedCount++

        val eventDetail = "onFriendAddRejected[好友申请被拒]"
        val eventData = "申请者账号: ${applicationInfo.applicantAccountId}accountId\n" +
                "申请留言: ${applicationInfo.postscript}\n" +
                "被申请者账号: ${applicationInfo.recipientAccountId}\n" +
                "操作者账号: ${applicationInfo.operatorAccountId}\n" +
                "时间: ${dateTimeFormatter.format(Date())}"

        addCallbackRecord("FRIEND_ADD_REJECTED", eventDetail, eventData)
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
            friendListener?.let { listener ->
                NIMClient.getService(V2NIMFriendService::class.java).removeFriendListener(listener)
                friendListener = null
                isListenerAdded = false
                
                addCallbackRecord(
                    "LISTENER_REMOVED",
                    "好友监听器移除成功",
                    "监听器已从好友服务中移除，停止监听好友相关事件"
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
        friendListener = null
        isListenerAdded = false
        listenStartTime = 0L
        callbackHistory.clear()
        statistics = CallbackStatistics()
    }
}