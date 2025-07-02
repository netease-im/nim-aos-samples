package com.netease.nim.samples.login.fragment

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.databinding.FragmentLogoutBinding
import com.netease.nim.samples.login.LoginActivity
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.V2NIMError
import com.netease.nimlib.sdk.v2.auth.V2NIMLoginService
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMLoginStatus

class LogoutFragment : BaseMethodExecuteFragment<FragmentLogoutBinding>() {

    private val handler = Handler(Looper.getMainLooper())
    private var countdownRunnable: Runnable? = null
    /**
     * 创建ViewBinding，由子类实现
     */
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentLogoutBinding {
        return FragmentLogoutBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateCurrentLoginStatus()
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
                V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGINED -> {
                    "当前状态: 已登录 (用户: ${loginUser ?: "未知"})"
                }
                V2NIMLoginStatus.V2NIM_LOGIN_STATUS_UNLOGIN -> {
                    "当前状态: 未登录"
                }
                V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGINING -> {
                    "当前状态: 登录中..."
                }
                else -> {
                    "当前状态: 未知状态"
                }
            }
            
            binding.tvCurrentLoginStatus.text = statusText
            
            // 如果未登录，提示用户
            if (loginStatus != V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGINED) {
                binding.tvLogoutStatus.text = "当前未登录，无需执行登出操作"
                binding.tvLogoutStatus.setTextColor(resources.getColor(android.R.color.holo_orange_dark))
            }
            
        } catch (e: Exception) {
            binding.tvCurrentLoginStatus.text = "获取登录状态失败: ${e.message}"
        }
    }

    /**
     * 发起登出请求
     */
    override fun onRequest() {
        val loginService = NIMClient.getService(V2NIMLoginService::class.java)
        
        // 检查当前登录状态
        if (loginService.loginStatus != V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGINED) {
            showToast("当前未登录，无需执行登出操作")
            activityViewModel.refresh("登出失败: 当前未登录状态")
            return
        }

        binding.tvLogoutStatus.text = "正在执行登出操作..."
        binding.tvLogoutStatus.setTextColor(resources.getColor(android.R.color.holo_blue_dark))
        
        activityViewModel.showLoadingDialog()
        
        loginService.logout(
            { 
                // 登出成功
                handleLogoutSuccess()
            },
            { error -> 
                // 登出失败
                handleLogoutFailure(error)
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 清理倒计时任务
        countdownRunnable?.let { handler.removeCallbacks(it) }
        countdownRunnable = null
    }

    /**
     * 处理登出成功
     */
    private fun handleLogoutSuccess() {
        activityViewModel.dismissLoadingDialog()

        binding.tvLogoutStatus.text = "登出成功，即将跳转到登录页面"
        binding.tvLogoutStatus.setTextColor(resources.getColor(android.R.color.holo_green_dark))

        // 更新登录状态显示
        updateCurrentLoginStatus()

        val resultText = buildSuccessResultText()
        activityViewModel.refresh(resultText)

        showToast("登出成功，即将跳转到登录页面")
        // 开始倒计时跳转
        startCountdownAndNavigate()
    }

    /**
     * 开始倒计时并跳转到登录页面
     */
    private fun startCountdownAndNavigate() {
        var countdown = 3 // 3秒倒计时

        countdownRunnable = object : Runnable {
            override fun run() {
                if (!isAdded || isDetached || activity == null) {
                    return
                }

                if (countdown > 0) {
                    binding.tvLogoutStatus.text = "登出成功，${countdown}秒后跳转到登录页面"
                    countdown--
                    handler.postDelayed(this, 1000) // 1秒后再次执行
                } else {
                    binding.tvLogoutStatus.text = "正在跳转到登录页面..."
                    navigateToLoginActivity()
                }
            }
        }

        countdownRunnable?.let { handler.post(it) }
    }

    /**
     * 跳转到登录页面并清空其他Activity
     */
    private fun navigateToLoginActivity() {
        try {
            // 检查Fragment是否还在活动状态
            if (!isAdded || isDetached || activity == null) {
                return
            }

            val intent = Intent(requireContext(), LoginActivity::class.java)
            // 清空任务栈，创建新的任务栈
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            // 添加额外的标志确保清空所有Activity
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

            startActivity(intent)

            // 结束当前Activity
            requireActivity().finish()

            // 确保进程完全重启（可选，根据需要）
            // android.os.Process.killProcess(android.os.Process.myPid())

        } catch (e: Exception) {
            // 如果跳转失败，至少显示错误信息
            showToast("跳转到登录页面失败: ${e.message}")

            // 尝试备用方案：直接结束当前Activity
            try {
                requireActivity().finish()
            } catch (ex: Exception) {
                // 忽略异常
            }
        }
    }

    /**
     * 处理登出失败
     */
    private fun handleLogoutFailure(error: V2NIMError) {
        activityViewModel.dismissLoadingDialog()
        
        binding.tvLogoutStatus.text = "登出失败: ${error.desc}"
        binding.tvLogoutStatus.setTextColor(resources.getColor(android.R.color.holo_red_dark))
        
        val resultText = buildFailureResultText(error)
        activityViewModel.refresh(resultText)
        
        showToast("登出失败: ${error.desc}")
    }

    /**
     * 构建成功结果文本
     */
    private fun buildSuccessResultText(): String {
        val sb = StringBuilder()

        sb.append("登出成功!\n\n")

        sb.append("操作详情:\n")
        sb.append("• 执行时间: ${System.currentTimeMillis()}\n")
        sb.append("• 连接状态: 已断开\n")
        sb.append("• 登录状态: 已登出\n\n")


        return sb.toString()
    }

    /**
     * 构建失败结果文本
     */
    private fun buildFailureResultText(error: V2NIMError): String {
        val sb = StringBuilder()
        
        sb.append("登出失败!\n\n")
        
        sb.append("错误信息:\n")
        sb.append("• 错误码: ${error.code}\n")
        sb.append("• 错误描述: ${error.desc}\n")
        sb.append("• 错误详情: ${error.detail ?: "无"}\n\n")
        
        sb.append("可能原因:\n")
        sb.append("• 网络连接异常\n")
        sb.append("• 服务器响应超时\n")
        sb.append("• SDK内部状态异常\n")
        sb.append("• 当前已处于登出状态\n\n")
        
        return sb.toString()
    }
}