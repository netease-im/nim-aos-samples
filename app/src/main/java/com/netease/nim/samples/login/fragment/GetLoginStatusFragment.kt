package com.netease.nim.samples.login.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.databinding.FragmentGetLoginStatusBinding
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.auth.V2NIMLoginService
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMLoginStatus

class GetLoginStatusFragment : BaseMethodExecuteFragment<FragmentGetLoginStatusBinding>() {


    /**
     * 创建ViewBinding，由子类实现
     */
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentGetLoginStatusBinding {
        return FragmentGetLoginStatusBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initStatusIndicators()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    /**
     * 初始化状态指示器
     */
    private fun initStatusIndicators() {
        // 初始化状态指示器的显示
        updateStatusIndicators(null)
    }


    /**
     * 更新状态指示器
     */
    private fun updateStatusIndicators(status: V2NIMLoginStatus?) {
        // 重置所有指示器
        binding.indicatorLogout.setBackgroundColor(resources.getColor(android.R.color.darker_gray))
        binding.indicatorLogined.setBackgroundColor(resources.getColor(android.R.color.darker_gray))
        binding.indicatorLogining.setBackgroundColor(resources.getColor(android.R.color.darker_gray))
        binding.indicatorUnlogin.setBackgroundColor(resources.getColor(android.R.color.darker_gray))

        // 根据状态点亮对应指示器
        when (status) {
            V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGOUT -> {
                binding.indicatorLogout.setBackgroundColor(resources.getColor(android.R.color.holo_red_dark))
            }
            V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGINED -> {
                binding.indicatorLogined.setBackgroundColor(resources.getColor(android.R.color.holo_green_dark))
            }
            V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGINING -> {
                binding.indicatorLogining.setBackgroundColor(resources.getColor(android.R.color.holo_blue_dark))
            }
            V2NIMLoginStatus.V2NIM_LOGIN_STATUS_UNLOGIN -> {
                binding.indicatorUnlogin.setBackgroundColor(resources.getColor(android.R.color.holo_orange_dark))
            }
            null -> {
                // 异常状态，所有指示器保持灰色
            }
        }
    }

    /**
     * 获取状态显示名称
     */
    private fun getLoginStatusDisplayName(status: V2NIMLoginStatus): String {
        return when (status) {
            V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGOUT -> "已登出"
            V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGINED -> "已登录"
            V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGINING -> "登录中"
            V2NIMLoginStatus.V2NIM_LOGIN_STATUS_UNLOGIN -> "未登录(重试中)"
            else -> "未知状态"
        }
    }


    /**
     * 发起请求 - 获取登录状态
     */
    override fun onRequest() {
        try {
            val loginService = NIMClient.getService(V2NIMLoginService::class.java)
            
            // 执行 getLoginStatus() 方法
            val loginStatus = loginService.loginStatus
            
            // 获取额外信息用于完整展示
            val loginUser = loginService.loginUser
            
            updateResult(loginStatus, loginUser, null)
            
        } catch (e: Exception) {
            updateResult(null, null, e)
        }
    }

    /**
     * 更新结果显示
     */
    private fun updateResult(loginStatus: V2NIMLoginStatus?, loginUser: String?, error: Exception?) {
        if (error != null) {
            val resultText = buildErrorResultText(error)
            activityViewModel.refresh(resultText)
            showToast("获取登录状态失败: ${error.message}")
            return
        }

        loginStatus?.let {
            updateStatusIndicators(loginStatus)
            val resultText = buildSuccessResultText(it, loginUser)
            activityViewModel.refresh(resultText)

            
            val toastMessage = "获取成功: ${getLoginStatusDisplayName(it)}"
            showToast(toastMessage)
        }
    }

    /**
     * 构建成功结果文本
     */
    private fun buildSuccessResultText(loginStatus: V2NIMLoginStatus, loginUser: String?): String {
        val sb = StringBuilder()

        sb.append("getLoginStatus() 调用成功!\n\n")

        sb.append("返回结果:\n")
        sb.append("• 登录状态: ${loginStatus.name}\n")
        sb.append("• 状态值: ${loginStatus.value}\n")
        sb.append("• 显示名称: ${getLoginStatusDisplayName(loginStatus)}\n")
        sb.append("• 返回类型: V2NIMLoginStatus\n")

        return sb.toString()
    }

    /**
     * 构建错误结果文本
     */
    private fun buildErrorResultText(error: Exception): String {
        val sb = StringBuilder()

        sb.append("getLoginStatus() 调用失败!\n\n")

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
     * 获取登录状态描述
     */
    private fun getLoginStatusDescription(status: V2NIMLoginStatus): String {
        return when (status) {
            V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGOUT -> 
                "登出状态，SDK初始化时或调用logout接口后，或登录已终止（被踢下线或遇到无法继续登录的错误）"
            V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGINED -> 
                "已登录状态，用户成功登录，可以正常使用IM功能"
            V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGINING -> 
                "登录中状态，包括连接到服务器和与服务器进行登录鉴权的过程"
            V2NIMLoginStatus.V2NIM_LOGIN_STATUS_UNLOGIN -> 
                "未登录状态，但登录未终止，SDK会尝试重新登录"
            else -> "未知登录状态"
        }
    }
}