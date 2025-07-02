package com.netease.nim.samples.login.common

import android.content.Context
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.V2NIMError
import com.netease.nimlib.sdk.v2.auth.V2NIMLoginListener
import com.netease.nimlib.sdk.v2.auth.V2NIMLoginService
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMLoginClientChange
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMLoginStatus
import com.netease.nimlib.sdk.v2.auth.model.V2NIMKickedOfflineDetail
import com.netease.nimlib.sdk.v2.auth.model.V2NIMLoginClient
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMKickedOfflineReason
import java.text.SimpleDateFormat
import java.util.*

/**
 * 全局登录监听器管理器
 * 用于统一管理登录状态监听，避免重复添加监听器
 */
object GlobalLoginListenerManager {

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
        val totalCallbackCount: Int,
        val loginStatusCount: Int,
        val loginFailedCount: Int,
        val kickedOfflineCount: Int,
        val clientChangedCount: Int
    )

    private val dateTimeFormatter = SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault())

    private var isListenerAdded = false
    private var listenStartTime = 0L
    private val callbackHistory = mutableListOf<CallbackRecord>()

    // 统计计数器
    private var totalCallbackCount = 0
    private var loginStatusCount = 0
    private var loginFailedCount = 0
    private var kickedOfflineCount = 0
    private var clientChangedCount = 0

    /**
     * 登录监听器实例
     */
    private val loginListener = object : V2NIMLoginListener {

        override fun onLoginStatus(status: V2NIMLoginStatus) {
            val statusDesc = when (status) {
                V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGINED -> "已登录"
                V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGOUT -> "已登出"
                V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGINING -> "登录中"
                V2NIMLoginStatus.V2NIM_LOGIN_STATUS_UNLOGIN -> "未登录"
                else -> "未知状态($status)"
            }

            addCallbackRecord(
                eventType = "onLoginStatus[登录状态变更]",
                eventDetail = "状态: $statusDesc",
                eventData = "LoginStatus: $status"
            )

            loginStatusCount++
            totalCallbackCount++
        }

        override fun onLoginFailed(error: V2NIMError) {
            addCallbackRecord(
                eventType = "onLoginFailed[登录失败]",
                eventDetail = "错误码: ${error.code}, 错误信息: ${error.desc}",
                eventData = "Error: ${error.code} - ${error.desc}"
            )

            loginFailedCount++
            totalCallbackCount++
        }

        override fun onKickedOffline(detail: V2NIMKickedOfflineDetail) {
            val reasonDesc = when (detail.reason) {
                V2NIMKickedOfflineReason.V2NIM_KICKED_OFFLINE_REASON_CLIENT_EXCLUSIVE -> "被另外一个客户端踢下线(互斥客户端)"
                V2NIMKickedOfflineReason.V2NIM_KICKED_OFFLINE_REASON_SERVER -> "被服务器踢下线"
                V2NIMKickedOfflineReason.V2NIM_KICKED_OFFLINE_REASON_CLIENT -> "被另外一个客户端手动选择踢下线"
                else -> "其他原因(${detail.reason})"
            }

            addCallbackRecord(
                eventType = "onKickedOffline[被踢下线]",
                eventDetail = "原因: $reasonDesc (${detail.reasonDesc ?: ""})",
                eventData = "Reason: ${detail.reason}, ClientType: ${detail.clientType}, CustomClientType: ${detail.customClientType ?: ""}"
            )

            kickedOfflineCount++
            totalCallbackCount++
        }

        override fun onLoginClientChanged(change: V2NIMLoginClientChange, clients: List<V2NIMLoginClient>) {
            val changeDesc = when (change) {
                V2NIMLoginClientChange.V2NIM_LOGIN_CLIENT_CHANGE_LOGIN -> "客户端登录"
                V2NIMLoginClientChange.V2NIM_LOGIN_CLIENT_CHANGE_LOGOUT -> "客户端登出"
                else -> "客户端变更($change)"
            }

            val clientsInfo = clients.map {
                "${it.type}${if (it.customClientType != null) "(${it.customClientType})" else ""}"
            }.joinToString(", ")

            addCallbackRecord(
                eventType = "onLoginClientChanged[登录客户端变更]",
                eventDetail = "$changeDesc, 当前客户端: $clientsInfo",
                eventData = "Change: $change, Clients: ${clients.size}个"
            )

            clientChangedCount++
            totalCallbackCount++
        }
    }

    /**
     * 添加登录监听器
     */
    fun addListener(context: Context): Boolean {
        return try {
            if (!isListenerAdded) {
                NIMClient.getService(V2NIMLoginService::class.java).addLoginListener(loginListener)
                isListenerAdded = true
                listenStartTime = System.currentTimeMillis()

                addCallbackRecord(
                    eventType = "系统事件",
                    eventDetail = "全局登录监听器添加成功",
                    eventData = "监听器已激活，开始监听登录状态变化"
                )
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            addCallbackRecord(
                eventType = "系统错误",
                eventDetail = "添加监听器失败: ${e.message}",
                eventData = "Exception: ${e.javaClass.simpleName}"
            )
            false
        }
    }

    /**
     * 移除登录监听器
     */
    fun removeListener(): Boolean {
        return try {
            if (isListenerAdded) {
                NIMClient.getService(V2NIMLoginService::class.java).removeLoginListener(loginListener)
                isListenerAdded = false
                
                addCallbackRecord(
                    eventType = "系统事件",
                    eventDetail = "全局登录监听器已移除",
                    eventData = "监听器已停用"
                )
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            addCallbackRecord(
                eventType = "系统错误",
                eventDetail = "移除监听器失败: ${e.message}",
                eventData = "Exception: ${e.javaClass.simpleName}"
            )
            false
        }
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
        
        // 插入到列表开头，保持最新的在前面
        callbackHistory.add(0, record)
        
        // 限制历史记录数量，防止内存溢出
        if (callbackHistory.size > 100) {
            callbackHistory.removeAt(callbackHistory.size - 1)
        }
    }

    /**
     * 获取监听器状态
     */
    fun isListenerAdded(): Boolean = isListenerAdded

    /**
     * 获取监听开始时间
     */
    fun getListenStartTime(): Long = listenStartTime

    /**
     * 获取最近的回调记录
     */
    fun getLatestCallback(): CallbackRecord? = callbackHistory.firstOrNull()

    /**
     * 获取回调历史记录
     */
    fun getCallbackHistory(): List<CallbackRecord> = callbackHistory.toList()

    /**
     * 获取统计信息
     */
    fun getStatistics(): Statistics {
        return Statistics(
            totalCallbackCount = totalCallbackCount,
            loginStatusCount = loginStatusCount,
            loginFailedCount = loginFailedCount,
            kickedOfflineCount = kickedOfflineCount,
            clientChangedCount = clientChangedCount
        )
    }

    /**
     * 清空历史记录
     */
    fun clearHistory() {
        callbackHistory.clear()
        totalCallbackCount = 0
        loginStatusCount = 0
        loginFailedCount = 0
        kickedOfflineCount = 0
        clientChangedCount = 0
    }
}