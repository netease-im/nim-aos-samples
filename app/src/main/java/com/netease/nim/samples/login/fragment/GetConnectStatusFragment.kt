package com.netease.nim.samples.login.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.databinding.FragmentGetConnectStatusBinding
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.auth.V2NIMLoginService
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMConnectStatus
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMLoginStatus

class GetConnectStatusFragment : BaseMethodExecuteFragment<FragmentGetConnectStatusBinding>() {

    /**
     * 创建ViewBinding，由子类实现
     */
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentGetConnectStatusBinding {
        return FragmentGetConnectStatusBinding.inflate(inflater, container, false)
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
            clearConnectStatusDisplay()
            showToast("已清空连接状态显示")
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
            val connectStatus = loginService.connectStatus
            
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
            binding.tvStatusTip.text = analyzeStatusRelation(loginStatus, connectStatus)
            binding.tvStatusTip.setTextColor(getStatusTipColor(loginStatus, connectStatus))
            
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
     * 清空连接状态显示
     */
    private fun clearConnectStatusDisplay() {
        binding.tvConnectDetailStatus.text = "已清空显示，可重新获取连接状态"
        binding.tvConnectDetailStatus.setTextColor(resources.getColor(android.R.color.holo_blue_dark))
        
        // 清空详情显示区域
        binding.tvConnectStatusValue.text = "--"
        binding.tvConnectStatusName.text = "--"
        binding.tvConnectStatusMeaning.text = "--"
        binding.tvConnectStatusSuggestion.text = "--"
        
        // 重置颜色
        val defaultColor = resources.getColor(android.R.color.black)
        binding.tvConnectStatusValue.setTextColor(defaultColor)
        binding.tvConnectStatusName.setTextColor(defaultColor)
        binding.tvConnectStatusMeaning.setTextColor(defaultColor)
        binding.tvConnectStatusSuggestion.setTextColor(defaultColor)
    }

    /**
     * 发起获取连接状态请求
     */
    override fun onRequest() {
        try {
            val loginService = NIMClient.getService(V2NIMLoginService::class.java)
            
            binding.tvConnectDetailStatus.text = "正在获取连接状态..."
            binding.tvConnectDetailStatus.setTextColor(resources.getColor(android.R.color.holo_blue_dark))
            
            // 执行 getConnectStatus() 方法
            val connectStatus = loginService.connectStatus
            
            // 获取登录状态用于结果分析
            val loginStatus = loginService.loginStatus
            val loginUser = loginService.loginUser
            
            updateResult(connectStatus, loginStatus, loginUser, null)
            
        } catch (e: Exception) {
            updateResult(null, null, null, e)
        }
    }

    /**
     * 更新结果显示
     */
    private fun updateResult(
        connectStatus: V2NIMConnectStatus?, 
        loginStatus: V2NIMLoginStatus?, 
        loginUser: String?, 
        error: Exception?
    ) {
        if (error != null) {
            val resultText = buildErrorResultText(error)
            activityViewModel.refresh(resultText)
            
            binding.tvConnectDetailStatus.text = "获取失败: ${error.message}"
            binding.tvConnectDetailStatus.setTextColor(resources.getColor(android.R.color.holo_red_dark))
            
            showToast("获取连接状态失败: ${error.message}")
            return
        }

        val resultText = buildSuccessResultText(connectStatus!!, loginStatus, loginUser)
        activityViewModel.refresh(resultText)
        
        // 更新详情显示
        updateConnectStatusDisplay(connectStatus)
        
        // 更新登录状态显示
        updateCurrentLoginStatus()
        
        val statusName = getConnectStatusDisplayName(connectStatus)
        showToast("获取成功: $statusName")
    }

    /**
     * 更新连接状态详情显示
     */
    private fun updateConnectStatusDisplay(connectStatus: V2NIMConnectStatus) {
        binding.tvConnectDetailStatus.text = "获取连接状态成功"
        binding.tvConnectDetailStatus.setTextColor(resources.getColor(android.R.color.holo_green_dark))
        
        // 显示详细信息
        val statusColor = getConnectStatusColor(connectStatus)
        
        binding.tvConnectStatusValue.text = "${connectStatus.value}"
        binding.tvConnectStatusName.text = getConnectStatusDisplayName(connectStatus)
        binding.tvConnectStatusMeaning.text = getConnectStatusMeaning(connectStatus)
        binding.tvConnectStatusSuggestion.text = getConnectStatusSuggestion(connectStatus)
        
        // 设置颜色
        binding.tvConnectStatusValue.setTextColor(statusColor)
        binding.tvConnectStatusName.setTextColor(statusColor)
        binding.tvConnectStatusMeaning.setTextColor(statusColor)
        binding.tvConnectStatusSuggestion.setTextColor(statusColor)
    }

    /**
     * 构建成功结果文本
     */
    private fun buildSuccessResultText(
        connectStatus: V2NIMConnectStatus, 
        loginStatus: V2NIMLoginStatus?, 
        loginUser: String?
    ): String {
        val sb = StringBuilder()

        sb.append("getConnectStatus() 调用成功!\n\n")

        sb.append("返回结果:\n")
        sb.append("• 连接状态: ${getConnectStatusDisplayName(connectStatus)}\n")
        sb.append("• 状态值: ${connectStatus.value}\n")
        sb.append("• 返回类型: V2NIMConnectStatus (枚举)\n")
        sb.append("• 调用时间: ${System.currentTimeMillis()}\n")

        sb.append("\n当前状态:\n")
        loginStatus?.let {
            sb.append("• 登录状态: ${getLoginStatusDisplayName(it)}\n")
        }
        sb.append("• 登录用户: ${loginUser ?: "无"}\n")
        sb.append("• 方法类型: 同步方法\n")

        sb.append("\n连接状态详情:\n")
        sb.append("• 状态名称: ${getConnectStatusDisplayName(connectStatus)}\n")
        sb.append("• 状态含义: ${getConnectStatusMeaning(connectStatus)}\n")
        sb.append("• 建议操作: ${getConnectStatusSuggestion(connectStatus)}\n")

        sb.append("\n结果分析:\n")
        loginStatus?.let {
            sb.append(analyzeStatusRelation(it, connectStatus))
        }

        sb.append("\n接口说明:\n")
        sb.append("• 总是返回有效的枚举值，不会为null\n")
        sb.append("• 状态实时反映当前连接情况\n")
        sb.append("• 可配合监听器实现状态变更监控\n")

        return sb.toString()
    }

    /**
     * 构建错误结果文本
     */
    private fun buildErrorResultText(error: Exception): String {
        val sb = StringBuilder()

        sb.append("getConnectStatus() 调用失败!\n\n")

        sb.append("异常信息:\n")
        sb.append("• 异常类型: ${error.javaClass.simpleName}\n")
        sb.append("• 异常消息: ${error.message ?: "未知异常"}\n")
        sb.append("• 调用时间: ${System.currentTimeMillis()}\n")

        sb.append("\n可能原因:\n")
        sb.append("• SDK未正确初始化\n")
        sb.append("• V2NIMLoginService服务获取失败\n")
        sb.append("• 系统内部异常\n")

        sb.append("\n解决建议:\n")
        sb.append("• 检查SDK初始化状态\n")
        sb.append("• 确认登录服务可用性\n")
        sb.append("• 查看详细异常日志\n")

        return sb.toString()
    }

    /**
     * 分析登录状态与连接状态的关系
     */
    private fun analyzeStatusRelation(loginStatus: V2NIMLoginStatus, connectStatus: V2NIMConnectStatus): String {
        return when {
            loginStatus == V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGINED && connectStatus == V2NIMConnectStatus.V2NIM_CONNECT_STATUS_CONNECTED -> {
                "✅ 理想状态：已登录且连接正常"
            }
            loginStatus == V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGINED && connectStatus == V2NIMConnectStatus.V2NIM_CONNECT_STATUS_CONNECTING -> {
                "🔄 重连中：已登录但正在重新连接"
            }
            loginStatus == V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGINED && connectStatus == V2NIMConnectStatus.V2NIM_CONNECT_STATUS_WAITING -> {
                "⏳ 等待重连：已登录但等待重连"
            }
            loginStatus == V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGINED && connectStatus == V2NIMConnectStatus.V2NIM_CONNECT_STATUS_DISCONNECTED -> {
                "⚠️ 异常状态：已登录但连接断开"
            }
            loginStatus == V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGINING && connectStatus == V2NIMConnectStatus.V2NIM_CONNECT_STATUS_CONNECTING -> {
                "🔄 登录中：正在登录且建立连接"
            }
            loginStatus == V2NIMLoginStatus.V2NIM_LOGIN_STATUS_UNLOGIN && connectStatus == V2NIMConnectStatus.V2NIM_CONNECT_STATUS_DISCONNECTED -> {
                "ℹ️ 正常状态：未登录且未连接"
            }
            else -> {
                "❓ 其他状态：登录(${getLoginStatusDisplayName(loginStatus)}) + 连接(${getConnectStatusDisplayName(connectStatus)})"
            }
        }
    }

    /**
     * 获取状态提示颜色
     */
    private fun getStatusTipColor(loginStatus: V2NIMLoginStatus, connectStatus: V2NIMConnectStatus): Int {
        return when {
            loginStatus == V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGINED && connectStatus == V2NIMConnectStatus.V2NIM_CONNECT_STATUS_CONNECTED -> {
                resources.getColor(android.R.color.holo_green_dark)
            }
            loginStatus == V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGINED && connectStatus != V2NIMConnectStatus.V2NIM_CONNECT_STATUS_CONNECTED -> {
                resources.getColor(android.R.color.holo_orange_dark)
            }
            else -> {
                resources.getColor(android.R.color.holo_blue_dark)
            }
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
     * 获取连接状态含义
     */
    private fun getConnectStatusMeaning(status: V2NIMConnectStatus): String {
        return when (status) {
            V2NIMConnectStatus.V2NIM_CONNECT_STATUS_DISCONNECTED -> "与服务器断开连接"
            V2NIMConnectStatus.V2NIM_CONNECT_STATUS_CONNECTED -> "与服务器正常连接"
            V2NIMConnectStatus.V2NIM_CONNECT_STATUS_CONNECTING -> "正在建立与服务器的连接"
            V2NIMConnectStatus.V2NIM_CONNECT_STATUS_WAITING -> "准备重新连接服务器"
        }
    }

    /**
     * 获取连接状态建议
     */
    private fun getConnectStatusSuggestion(status: V2NIMConnectStatus): String {
        return when (status) {
            V2NIMConnectStatus.V2NIM_CONNECT_STATUS_DISCONNECTED -> "检查网络连接，尝试重新登录"
            V2NIMConnectStatus.V2NIM_CONNECT_STATUS_CONNECTED -> "连接正常，可以正常使用"
            V2NIMConnectStatus.V2NIM_CONNECT_STATUS_CONNECTING -> "正在连接中，请稍等"
            V2NIMConnectStatus.V2NIM_CONNECT_STATUS_WAITING -> "等待自动重连，检查网络状态"
        }
    }

    /**
     * 获取连接状态颜色
     */
    private fun getConnectStatusColor(status: V2NIMConnectStatus): Int {
        return when (status) {
            V2NIMConnectStatus.V2NIM_CONNECT_STATUS_CONNECTED -> 
                resources.getColor(android.R.color.holo_green_dark)
            V2NIMConnectStatus.V2NIM_CONNECT_STATUS_CONNECTING -> 
                resources.getColor(android.R.color.holo_blue_dark)
            V2NIMConnectStatus.V2NIM_CONNECT_STATUS_WAITING -> 
                resources.getColor(android.R.color.holo_orange_dark)
            V2NIMConnectStatus.V2NIM_CONNECT_STATUS_DISCONNECTED -> 
                resources.getColor(android.R.color.holo_red_dark)
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