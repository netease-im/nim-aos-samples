package com.netease.nim.samples.login.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.databinding.FragmentGetCurrentLoginClientBinding
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.auth.V2NIMLoginService
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMLoginStatus
import com.netease.nimlib.sdk.v2.auth.model.V2NIMLoginClient
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMLoginClientType
import java.text.SimpleDateFormat
import java.util.*

class GetCurrentLoginClientFragment : BaseMethodExecuteFragment<FragmentGetCurrentLoginClientBinding>() {

    /**
     * 创建ViewBinding，由子类实现
     */
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentGetCurrentLoginClientBinding {
        return FragmentGetCurrentLoginClientBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initClickListeners()
        updateLoginStatusDisplay()
    }

    /**
     * 初始化点击监听
     */
    private fun initClickListeners() {
        // 刷新登录状态按钮
        binding.btnRefreshLoginStatus.setOnClickListener {
            updateLoginStatusDisplay()
            showToast("已刷新登录状态")
        }

        // 清空显示按钮
        binding.btnClearDisplay.setOnClickListener {
            clearClientDisplay()
            showToast("已清空显示")
        }
    }

    /**
     * 清空客户端信息显示
     */
    private fun clearClientDisplay() {
        binding.tvClientType.text = "--"
        binding.tvClientOs.text = "--"
        binding.tvClientId.text = "--"
        binding.tvLoginTimestamp.text = "--"
        binding.tvCustomTag.text = "--"
        binding.tvCustomClientType.text = "--"
    }

    /**
     * 更新登录状态显示
     */
    private fun updateLoginStatusDisplay() {
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

            binding.tvLoginStatus.text = "登录状态: $statusText"
            binding.tvLoginUser.text = "登录用户: ${loginUser ?: "无"}"

            // 设置状态颜色
            val textColor = when (loginStatus) {
                V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGINED -> resources.getColor(android.R.color.holo_green_dark)
                V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGOUT -> resources.getColor(android.R.color.holo_red_dark)
                V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGINING -> resources.getColor(android.R.color.holo_blue_dark)
                else -> resources.getColor(android.R.color.holo_orange_dark)
            }

            binding.tvLoginStatus.setTextColor(textColor)
            binding.tvLoginUser.setTextColor(textColor)

            // 提示信息
            if (loginStatus != V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGINED) {
                binding.tvLoginTip.text = "仅在登录成功后才能获取客户端信息"
                binding.tvLoginTip.setTextColor(resources.getColor(android.R.color.holo_orange_dark))
            } else {
                binding.tvLoginTip.text = "当前已登录，可以获取客户端信息"
                binding.tvLoginTip.setTextColor(resources.getColor(android.R.color.holo_green_dark))
            }

        } catch (e: Exception) {
            binding.tvLoginStatus.text = "获取状态失败: ${e.message}"
            binding.tvLoginUser.text = "获取用户失败"
            binding.tvLoginTip.text = "状态获取异常"
            
            val errorColor = resources.getColor(android.R.color.holo_red_dark)
            binding.tvLoginStatus.setTextColor(errorColor)
            binding.tvLoginUser.setTextColor(errorColor)
            binding.tvLoginTip.setTextColor(errorColor)
        }
    }

    /**
     * 发起请求 - 获取当前登录客户端信息
     */
    override fun onRequest() {
        try {
            val loginService = NIMClient.getService(V2NIMLoginService::class.java)
            
            // 执行 getCurrentLoginClient() 方法
            val loginClient = loginService.currentLoginClient
            
            // 获取登录状态用于结果分析
            val loginStatus = loginService.loginStatus
            val loginUser = loginService.loginUser
            
            updateResult(loginClient, loginStatus, loginUser, null)
            
        } catch (e: Exception) {
            updateResult(null, null, null, e)
        }
    }

    /**
     * 更新结果显示
     */
    private fun updateResult(
        loginClient: V2NIMLoginClient?, 
        loginStatus: V2NIMLoginStatus?, 
        loginUser: String?, 
        error: Exception?
    ) {
        if (error != null) {
            val resultText = buildErrorResultText(error)
            activityViewModel.refresh(resultText)
            showToast("获取客户端信息失败: ${error.message}")
            return
        }

        val resultText = buildSuccessResultText(loginClient, loginStatus, loginUser)
        activityViewModel.refresh(resultText)
        
        // 更新界面客户端信息显示
        updateClientInfoDisplay(loginClient)
        
        // 更新登录状态显示
        updateLoginStatusDisplay()
        
        val toastMessage = if (loginClient != null) {
            "获取成功: ${getClientTypeDisplayName(loginClient.type)}"
        } else {
            "获取成功: 当前无客户端信息"
        }
        showToast(toastMessage)
    }

    /**
     * 更新客户端信息显示
     */
    private fun updateClientInfoDisplay(loginClient: V2NIMLoginClient?) {
        if (loginClient == null) {
            clearClientDisplay()
            return
        }

        binding.tvClientType.text = "${getClientTypeDisplayName(loginClient.type)} (${loginClient.type.value})"
        binding.tvClientOs.text = loginClient.os ?: "未知"
        binding.tvClientId.text = loginClient.clientId ?: "未知"
        
        // 格式化时间戳
        val timestamp = loginClient.timestamp
        val formattedTime = if (timestamp > 0) {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            sdf.format(Date(timestamp))
        } else {
            "未知"
        }
        binding.tvLoginTimestamp.text = "$formattedTime ($timestamp)"
        
        binding.tvCustomTag.text = loginClient.customTag ?: "无"
        binding.tvCustomClientType.text = loginClient.customClientType.toString()
    }

    /**
     * 构建成功结果文本
     */
    private fun buildSuccessResultText(
        loginClient: V2NIMLoginClient?, 
        loginStatus: V2NIMLoginStatus?, 
        loginUser: String?
    ): String {
        val sb = StringBuilder()

        sb.append("getCurrentLoginClient() 调用成功!\n\n")

        sb.append("返回结果:\n")
        if (loginClient != null) {
            sb.append("• 客户端类型: ${getClientTypeDisplayName(loginClient.type)}\n")
            sb.append("• 类型值: ${loginClient.type.value}\n")
            sb.append("• 操作系统: ${loginClient.os}\n")
            sb.append("• 客户端ID: ${loginClient.clientId}\n")
            sb.append("• 登录时间: ${formatTimestamp(loginClient.timestamp)}\n")
            sb.append("• 自定义标签: ${loginClient.customTag ?: "无"}\n")
            sb.append("• 自定义客户端类型: ${loginClient.customClientType}\n")
            sb.append("• 返回类型: V2NIMLoginClient\n")
        } else {
            sb.append("• 客户端信息: null\n")
            sb.append("• 返回类型: V2NIMLoginClient (可为null)\n")
            sb.append("• 说明: 当前未登录或登录状态异常\n")
        }

        sb.append("\n当前状态:\n")
        loginStatus?.let {
            sb.append("• 登录状态: ${getLoginStatusDisplayName(it)}\n")
        }
        sb.append("• 登录用户: ${loginUser ?: "无"}\n")
        sb.append("• 调用时间: ${System.currentTimeMillis()}\n")
        sb.append("• 方法类型: 同步方法\n")

        sb.append("\n结果分析:\n")
        sb.append(analyzeResult(loginClient, loginStatus))

        return sb.toString()
    }

    /**
     * 构建错误结果文本
     */
    private fun buildErrorResultText(error: Exception): String {
        val sb = StringBuilder()

        sb.append("getCurrentLoginClient() 调用失败!\n\n")

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
     * 分析结果
     */
    private fun analyzeResult(loginClient: V2NIMLoginClient?, loginStatus: V2NIMLoginStatus?): String {
        return when {
            loginStatus != V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGINED -> {
                "🔴 当前未登录状态，无法获取客户端信息"
            }
            loginClient == null -> {
                "⚠️ 已登录但客户端信息为空，可能是SDK内部状态异常"
            }
            else -> {
                "✅ 正常状态：成功获取当前登录客户端信息"
            }
        }
    }

    /**
     * 获取客户端类型显示名称
     */
    private fun getClientTypeDisplayName(type: V2NIMLoginClientType): String {
        return when (type) {
            V2NIMLoginClientType.V2NIM_LOGIN_CLIENT_TYPE_ANDROID -> "Android"
            V2NIMLoginClientType.V2NIM_LOGIN_CLIENT_TYPE_IOS -> "iOS"
            V2NIMLoginClientType.V2NIM_LOGIN_CLIENT_TYPE_PC -> "PC"
            V2NIMLoginClientType.V2NIM_LOGIN_CLIENT_TYPE_WINPHONE -> "Windows Phone"
            V2NIMLoginClientType.V2NIM_LOGIN_CLIENT_TYPE_WEB -> "Web"
            V2NIMLoginClientType.V2NIM_LOGIN_CLIENT_TYPE_RESTFUL -> "REST API"
            V2NIMLoginClientType.V2NIM_LOGIN_CLIENT_TYPE_UNKNOWN -> "未知类型"
            else -> "未知类型"
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

    /**
     * 格式化时间戳
     */
    private fun formatTimestamp(timestamp: Long): String {
        return if (timestamp > 0) {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            "${sdf.format(Date(timestamp))} ($timestamp)"
        } else {
            "未知时间 ($timestamp)"
        }
    }
}