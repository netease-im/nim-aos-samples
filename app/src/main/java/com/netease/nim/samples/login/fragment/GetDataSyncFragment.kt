package com.netease.nim.samples.login.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.databinding.FragmentGetDataSyncBinding
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.auth.V2NIMLoginService
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMConnectStatus
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMLoginStatus
import com.netease.nimlib.sdk.v2.auth.model.V2NIMDataSyncDetail
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMDataSyncType
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMDataSyncState

class GetDataSyncFragment : BaseMethodExecuteFragment<FragmentGetDataSyncBinding>() {

    /**
     * 创建ViewBinding，由子类实现
     */
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentGetDataSyncBinding {
        return FragmentGetDataSyncBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initClickListeners()
        updateCurrentLoginStatus()
    }

    /**
     * 初始化点击监听
     */
    private fun initClickListeners() {
        // 刷新状态按钮
        binding.btnRefreshStatus.setOnClickListener {
            updateCurrentLoginStatus()
            showToast("已刷新登录状态")
        }

        // 清空详情按钮
        binding.btnClearDetail.setOnClickListener {
            clearDataSyncDisplay()
            showToast("已清空数据同步详情显示")
        }
    }

    /**
     * 更新当前登录状态显示
     */
    private fun updateCurrentLoginStatus() {
        try {
            val loginService = NIMClient.getService(V2NIMLoginService::class.java)
            val loginStatus = loginService.loginStatus
            val loginUser = loginService.loginUser
            
            val statusText = when (loginStatus) {
                V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGINED -> "已登录"
                V2NIMLoginStatus.V2NIM_LOGIN_STATUS_UNLOGIN -> "未登录"
                V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGINING -> "登录中"
                V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGOUT -> "已登出"
                else -> "未知状态"
            }
            
            binding.tvCurrentLoginStatus.text = "当前状态: $statusText"
            binding.tvCurrentLoginUser.text = "当前用户: ${loginUser ?: "无"}"
            
            // 设置状态颜色
            val loginColor = when (loginStatus) {
                V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGINED -> resources.getColor(android.R.color.holo_green_dark)
                V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGOUT -> resources.getColor(android.R.color.holo_red_dark)
                V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGINING -> resources.getColor(android.R.color.holo_blue_dark)
                else -> resources.getColor(android.R.color.holo_orange_dark)
            }
            
            binding.tvCurrentLoginStatus.setTextColor(loginColor)
            binding.tvCurrentLoginUser.setTextColor(loginColor)
            
            // 提示信息
            val tipText = if (loginStatus == V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGINED) {
                "✅ 已登录，可以获取数据同步项"
            } else {
                "⚠️ 未登录，需要先登录才能获取数据同步项"
            }
            binding.tvStatusTip.text = tipText
            binding.tvStatusTip.setTextColor(loginColor)
            
        } catch (e: Exception) {
            binding.tvCurrentLoginStatus.text = "获取登录状态失败: ${e.message}"
            binding.tvCurrentLoginUser.text = "获取用户失败"
            binding.tvStatusTip.text = "状态获取异常"
            
            val errorColor = resources.getColor(android.R.color.holo_red_dark)
            binding.tvCurrentLoginStatus.setTextColor(errorColor)
            binding.tvCurrentLoginUser.setTextColor(errorColor)
            binding.tvStatusTip.setTextColor(errorColor)
        }
    }

    /**
     * 清空数据同步详情显示
     */
    private fun clearDataSyncDisplay() {
        binding.tvDataSyncStatus.text = "已清空显示，可重新获取数据同步项"
        binding.tvDataSyncStatus.setTextColor(resources.getColor(android.R.color.holo_blue_dark))
        
        // 清空详情显示区域
        binding.tvSyncItemCount.text = "--"
        binding.tvSyncCompletionStatus.text = "--"
        binding.tvSyncProgress.text = "--"
        binding.tvSyncDetails.text = "--"
        
        // 重置颜色
        val defaultColor = resources.getColor(android.R.color.black)
        binding.tvSyncItemCount.setTextColor(defaultColor)
        binding.tvSyncCompletionStatus.setTextColor(defaultColor)
        binding.tvSyncProgress.setTextColor(defaultColor)
        binding.tvSyncDetails.setTextColor(defaultColor)
    }

    /**
     * 发起获取数据同步项请求
     */
    override fun onRequest() {
        try {
            val loginService = NIMClient.getService(V2NIMLoginService::class.java)
            
            binding.tvDataSyncStatus.text = "正在获取数据同步项..."
            binding.tvDataSyncStatus.setTextColor(resources.getColor(android.R.color.holo_blue_dark))
            
            // 执行 getDataSync() 方法
            val dataSyncList = loginService.dataSync
            
            // 获取登录状态用于结果分析
            val loginStatus = loginService.loginStatus
            val loginUser = loginService.loginUser
            
            updateResult(dataSyncList, loginStatus, loginUser, null)
            
        } catch (e: Exception) {
            updateResult(null, null, null, e)
        }
    }

    /**
     * 更新结果显示
     */
    private fun updateResult(
        dataSyncList: List<V2NIMDataSyncDetail>?, 
        loginStatus: V2NIMLoginStatus?, 
        loginUser: String?, 
        error: Exception?
    ) {
        if (error != null) {
            val resultText = buildErrorResultText(error)
            activityViewModel.refresh(resultText)
            
            binding.tvDataSyncStatus.text = "获取失败: ${error.message}"
            binding.tvDataSyncStatus.setTextColor(resources.getColor(android.R.color.holo_red_dark))
            
            showToast("获取数据同步项失败: ${error.message}")
            return
        }

        val resultText = buildSuccessResultText(dataSyncList!!, loginStatus, loginUser)
        activityViewModel.refresh(resultText)
        
        // 更新详情显示
        updateDataSyncDisplay(dataSyncList)
        
        // 更新登录状态显示
        updateCurrentLoginStatus()
        
        showToast("获取成功: 共${dataSyncList.size}个同步项")
    }

    /**
     * 更新数据同步详情显示
     */
    private fun updateDataSyncDisplay(dataSyncList: List<V2NIMDataSyncDetail>) {
        binding.tvDataSyncStatus.text = "获取数据同步项成功"
        binding.tvDataSyncStatus.setTextColor(resources.getColor(android.R.color.holo_green_dark))
        
        val totalCount = dataSyncList.size
        val completedCount = dataSyncList.count { it.state == V2NIMDataSyncState.V2NIM_DATA_SYNC_STATE_COMPLETED }
        val syncingCount = dataSyncList.count { it.state == V2NIMDataSyncState.V2NIM_DATA_SYNC_STATE_SYNCING }
        val waitingCount = dataSyncList.count { it.state == V2NIMDataSyncState.V2NIM_DATA_SYNC_STATE_WAITING }
        
        // 显示统计信息
        binding.tvSyncItemCount.text = "$totalCount"
        binding.tvSyncItemCount.setTextColor(resources.getColor(android.R.color.holo_blue_dark))
        
        // 完成状态
        val completionText = if (totalCount == 0) {
            "无同步项"
        } else if (completedCount == totalCount) {
            "全部完成 ✅"
        } else {
            "部分完成 ($completedCount/$totalCount)"
        }
        binding.tvSyncCompletionStatus.text = completionText
        val completionColor = when {
            totalCount == 0 -> resources.getColor(android.R.color.holo_orange_dark)
            completedCount == totalCount -> resources.getColor(android.R.color.holo_green_dark)
            else -> resources.getColor(android.R.color.holo_orange_dark)
        }
        binding.tvSyncCompletionStatus.setTextColor(completionColor)
        
        // 同步进度
        val progressText = if (totalCount == 0) {
            "无进度信息"
        } else {
            val progressPercent = (completedCount * 100) / totalCount
            "$progressPercent% (完成:$completedCount, 同步中:$syncingCount, 等待:$waitingCount)"
        }
        binding.tvSyncProgress.text = progressText
        binding.tvSyncProgress.setTextColor(completionColor)
        
        // 详细信息
        val detailsText = if (dataSyncList.isEmpty()) {
            "暂无数据同步项"
        } else {
            buildString {
                dataSyncList.forEachIndexed { index, detail ->
                    append("${index + 1}. ${getDataSyncTypeDisplayName(detail.type)} - ${getDataSyncStateDisplayName(detail.state)}")
                    if (index < dataSyncList.size - 1) append("\n")
                }
            }
        }
        binding.tvSyncDetails.text = detailsText
        binding.tvSyncDetails.setTextColor(resources.getColor(android.R.color.black))
    }

    /**
     * 构建成功结果文本
     */
    private fun buildSuccessResultText(
        dataSyncList: List<V2NIMDataSyncDetail>, 
        loginStatus: V2NIMLoginStatus?, 
        loginUser: String?
    ): String {
        val sb = StringBuilder()

        sb.append("getDataSync() 调用成功!\n\n")

        sb.append("返回结果:\n")
        sb.append("• 同步项数量: ${dataSyncList.size}\n")
        sb.append("• 返回类型: List<V2NIMDataSyncDetail>\n")
        sb.append("• 调用时间: ${System.currentTimeMillis()}\n")

        sb.append("\n当前状态:\n")
        loginStatus?.let {
            sb.append("• 登录状态: ${getLoginStatusDisplayName(it)}\n")
        }
        sb.append("• 登录用户: ${loginUser ?: "无"}\n")
        sb.append("• 方法类型: 同步方法\n")

        if (dataSyncList.isNotEmpty()) {
            sb.append("\n数据同步详情:\n")
            
            // 统计信息
            val completedCount = dataSyncList.count { it.state == V2NIMDataSyncState.V2NIM_DATA_SYNC_STATE_COMPLETED }
            val syncingCount = dataSyncList.count { it.state == V2NIMDataSyncState.V2NIM_DATA_SYNC_STATE_SYNCING }
            val waitingCount = dataSyncList.count { it.state == V2NIMDataSyncState.V2NIM_DATA_SYNC_STATE_WAITING }
            
            sb.append("• 总计: ${dataSyncList.size} 个同步项\n")
            sb.append("• 已完成: $completedCount 个\n")
            sb.append("• 同步中: $syncingCount 个\n")
            sb.append("• 等待中: $waitingCount 个\n")
            
            val progressPercent = if (dataSyncList.isNotEmpty()) (completedCount * 100) / dataSyncList.size else 0
            sb.append("• 完成度: $progressPercent%\n")
            
            sb.append("\n各同步项详情:\n")
            dataSyncList.forEachIndexed { index, detail ->
                sb.append("${index + 1}. 类型: ${getDataSyncTypeDisplayName(detail.type)} (${detail.type.value})\n")
                sb.append("   状态: ${getDataSyncStateDisplayName(detail.state)} (${detail.state.value})\n")
                sb.append("   含义: ${getDataSyncTypeMeaning(detail.type)}\n")
                if (index < dataSyncList.size - 1) sb.append("\n")
            }
        } else {
            sb.append("\n数据同步详情:\n")
            sb.append("• 当前无数据同步项\n")
            sb.append("• 可能原因: 未登录、数据已全部同步或SDK未初始化完成\n")
        }

        sb.append("\n接口说明:\n")
        sb.append("• 返回当前所有数据同步项的列表\n")
        sb.append("• 列表可能为空，表示无同步项或同步已完成\n")
        sb.append("• 每个同步项包含类型和状态信息\n")
        sb.append("• 可用于监控数据同步进度\n")

        return sb.toString()
    }

    /**
     * 构建错误结果文本
     */
    private fun buildErrorResultText(error: Exception): String {
        val sb = StringBuilder()

        sb.append("getDataSync() 调用失败!\n\n")

        sb.append("异常信息:\n")
        sb.append("• 异常类型: ${error.javaClass.simpleName}\n")
        sb.append("• 异常消息: ${error.message ?: "未知异常"}\n")
        sb.append("• 调用时间: ${System.currentTimeMillis()}\n")

        sb.append("\n可能原因:\n")
        sb.append("• SDK未正确初始化\n")
        sb.append("• V2NIMLoginService服务获取失败\n")
        sb.append("• 用户未登录或登录状态异常\n")
        sb.append("• 系统内部异常\n")

        sb.append("\n解决建议:\n")
        sb.append("• 检查SDK初始化状态\n")
        sb.append("• 确认用户已成功登录\n")
        sb.append("• 确认登录服务可用性\n")
        sb.append("• 查看详细异常日志\n")

        return sb.toString()
    }

    /**
     * 获取数据同步类型显示名称
     */
    private fun getDataSyncTypeDisplayName(type: V2NIMDataSyncType): String {
        return when (type) {
            V2NIMDataSyncType.V2NIM_DATA_SYNC_MAIN -> "同步主数据"
            V2NIMDataSyncType.V2NIM_DATA_SYNC_TEAM_MEMBER -> "同步群组成员"
            V2NIMDataSyncType.V2NIM_DATA_SYNC_SUPER_TEAM_MEMBER -> "同步超大群组成员"
        }
    }

    /**
     * 获取数据同步类型含义
     */
    private fun getDataSyncTypeMeaning(type: V2NIMDataSyncType): String {
        return when (type) {
            V2NIMDataSyncType.V2NIM_DATA_SYNC_MAIN -> "同步用户主要数据如好友、消息等"
            V2NIMDataSyncType.V2NIM_DATA_SYNC_TEAM_MEMBER -> "同步普通群组成员信息"
            V2NIMDataSyncType.V2NIM_DATA_SYNC_SUPER_TEAM_MEMBER -> "同步超大群组成员信息"
        }
    }

    /**
     * 获取数据同步状态显示名称
     */
    private fun getDataSyncStateDisplayName(state: V2NIMDataSyncState): String {
        return when (state) {
            V2NIMDataSyncState.V2NIM_DATA_SYNC_STATE_WAITING -> "等待同步"
            V2NIMDataSyncState.V2NIM_DATA_SYNC_STATE_SYNCING -> "同步中"
            V2NIMDataSyncState.V2NIM_DATA_SYNC_STATE_COMPLETED -> "同步完成"
        }
    }

    /**
     * 获取连接状态显示名称
     */
    private fun getConnectStatusDisplayName(status: V2NIMConnectStatus): String {
        return when (status) {
            V2NIMConnectStatus.V2NIM_CONNECT_STATUS_DISCONNECTED -> "未连接"
            V2NIMConnectStatus.V2NIM_CONNECT_STATUS_CONNECTED -> "已连接"
            V2NIMConnectStatus.V2NIM_CONNECT_STATUS_CONNECTING -> "连接中"
            V2NIMConnectStatus.V2NIM_CONNECT_STATUS_WAITING -> "等待重连"
        }
    }

    /**
     * 获取登录状态显示名称
     */
    private fun getLoginStatusDisplayName(status: V2NIMLoginStatus): String {
        return when (status) {
            V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGINED -> "已登录"
            V2NIMLoginStatus.V2NIM_LOGIN_STATUS_UNLOGIN -> "未登录"
            V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGINING -> "登录中"
            V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGOUT -> "已登出"
            else -> "未知状态"
        }
    }
}