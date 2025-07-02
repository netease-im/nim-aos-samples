package com.netease.nim.samples.login.common

import android.content.Context
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.V2NIMError
import com.netease.nimlib.sdk.v2.auth.V2NIMLoginDetailListener
import com.netease.nimlib.sdk.v2.auth.V2NIMLoginService
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMConnectStatus
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMDataSyncState
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMDataSyncType
import java.text.SimpleDateFormat
import java.util.*

/**
 * 全局登录详情监听器管理器
 * 用于统一管理登录详情状态监听，避免重复添加监听器
 */
object GlobalLoginDetailListenerManager {

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
        val connectStatusCount: Int,
        val disconnectedCount: Int,
        val connectFailedCount: Int,
        val dataSyncCount: Int
    )

    private val dateTimeFormatter = SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault())
    
    private var isListenerAdded = false
    private var listenStartTime = 0L
    private val callbackHistory = mutableListOf<CallbackRecord>()
    
    // 统计计数器
    private var totalCallbackCount = 0
    private var connectStatusCount = 0
    private var disconnectedCount = 0
    private var connectFailedCount = 0
    private var dataSyncCount = 0

    /**
     * 登录详情监听器实例
     */
    private val loginDetailListener = object : V2NIMLoginDetailListener {
        
        override fun onConnectStatus(status: V2NIMConnectStatus) {
            val statusDesc = when (status) {
                V2NIMConnectStatus.V2NIM_CONNECT_STATUS_DISCONNECTED -> "未连接"
                V2NIMConnectStatus.V2NIM_CONNECT_STATUS_CONNECTED -> "已连接"
                V2NIMConnectStatus.V2NIM_CONNECT_STATUS_CONNECTING -> "连接中"
                V2NIMConnectStatus.V2NIM_CONNECT_STATUS_WAITING -> "等待重连"
                else -> "未知状态($status)"
            }
            
            addCallbackRecord(
                eventType = "onConnectStatus[连接状态变更]",
                eventDetail = "状态: $statusDesc",
                eventData = "ConnectStatus: $status"
            )
            
            connectStatusCount++
            totalCallbackCount++
        }

        override fun onDisconnected(error: V2NIMError) {
            addCallbackRecord(
                eventType = "onDisconnected[连接断开]",
                eventDetail = "错误码: ${error.code}, 错误信息: ${error.desc}",
                eventData = "Error: ${error.code} - ${error.desc}"
            )
            
            disconnectedCount++
            totalCallbackCount++
        }

        override fun onConnectFailed(error: V2NIMError) {
            addCallbackRecord(
                eventType = "onConnectFailed[连接失败]",
                eventDetail = "错误码: ${error.code}, 错误信息: ${error.desc}",
                eventData = "Error: ${error.code} - ${error.desc}"
            )
            
            connectFailedCount++
            totalCallbackCount++
        }

        override fun onDataSync(type: V2NIMDataSyncType, state: V2NIMDataSyncState, error: V2NIMError?) {
            val typeDesc = when (type) {
                V2NIMDataSyncType.V2NIM_DATA_SYNC_MAIN -> "同步主数据"
                V2NIMDataSyncType.V2NIM_DATA_SYNC_TEAM_MEMBER -> "同步群组成员"
                V2NIMDataSyncType.V2NIM_DATA_SYNC_SUPER_TEAM_MEMBER -> "同步超大群组成员"
                else -> "未知类型($type)"
            }
            
            val stateDesc = when (state) {
                V2NIMDataSyncState.V2NIM_DATA_SYNC_STATE_WAITING -> "等待同步"
                V2NIMDataSyncState.V2NIM_DATA_SYNC_STATE_SYNCING -> "同步中"
                V2NIMDataSyncState.V2NIM_DATA_SYNC_STATE_COMPLETED -> "同步完成"
                else -> "未知状态($state)"
            }
            
            val errorInfo = if (error != null) {
                " (错误: ${error.code} - ${error.desc})"
            } else {
                ""
            }
            
            addCallbackRecord(
                eventType = "onDataSync[数据同步]",
                eventDetail = "$typeDesc - $stateDesc$errorInfo",
                eventData = "Type: $type, State: $state${if (error != null) ", Error: ${error.code}" else ""}"
            )
            
            dataSyncCount++
            totalCallbackCount++
        }
    }

    /**
     * 添加登录详情监听器
     */
    fun addListener(context: Context): Boolean {
        return try {
            if (!isListenerAdded) {
                NIMClient.getService(V2NIMLoginService::class.java).addLoginDetailListener(loginDetailListener)
                isListenerAdded = true
                listenStartTime = System.currentTimeMillis()
                
                addCallbackRecord(
                    eventType = "系统事件",
                    eventDetail = "全局登录详情监听器添加成功",
                    eventData = "监听器已激活，开始监听连接状态和数据同步"
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
     * 移除登录详情监听器
     */
    fun removeListener(): Boolean {
        return try {
            if (isListenerAdded) {
                NIMClient.getService(V2NIMLoginService::class.java).removeLoginDetailListener(loginDetailListener)
                isListenerAdded = false
                
                addCallbackRecord(
                    eventType = "系统事件",
                    eventDetail = "全局登录详情监听器已移除",
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
            connectStatusCount = connectStatusCount,
            disconnectedCount = disconnectedCount,
            connectFailedCount = connectFailedCount,
            dataSyncCount = dataSyncCount
        )
    }

    /**
     * 清空历史记录
     */
    fun clearHistory() {
        callbackHistory.clear()
        totalCallbackCount = 0
        connectStatusCount = 0
        disconnectedCount = 0
        connectFailedCount = 0
        dataSyncCount = 0
    }
}