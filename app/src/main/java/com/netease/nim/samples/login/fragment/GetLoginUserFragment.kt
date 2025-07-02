package com.netease.nim.samples.login.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.databinding.FragmentGetLoginUserBinding
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.auth.V2NIMLoginService
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMLoginStatus

class GetLoginUserFragment : BaseMethodExecuteFragment<FragmentGetLoginUserBinding>() {

    /**
     * 创建ViewBinding，由子类实现
     */
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentGetLoginUserBinding {
        return FragmentGetLoginUserBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }




    /**
     * 发起请求 - 获取当前登录用户
     */
    override fun onRequest() {
        try {
            val loginService = NIMClient.getService(V2NIMLoginService::class.java)
            
            // 执行 getLoginUser() 方法
            val loginUser = loginService.loginUser

            
            updateResult(loginUser,null)
            
        } catch (e: Exception) {
            updateResult(null, e)
        }
    }

    /**
     * 更新结果显示
     */
    private fun updateResult(loginUser: String?, error: Exception?) {
        if (error != null) {
            val resultText = buildErrorResultText(error)
            activityViewModel.refresh(resultText)
            showToast("获取登录用户失败: ${error.message}")
            return
        }

        val resultText = buildSuccessResultText(loginUser)
        activityViewModel.refresh(resultText)

        
        val toastMessage = if (loginUser != null) {
            "获取成功: $loginUser"
        } else {
            "获取成功: 当前无登录用户"
        }
        showToast(toastMessage)
    }

    /**
     * 构建成功结果文本
     */
    private fun buildSuccessResultText(loginUser: String?): String {
        val sb = StringBuilder()

        sb.append("getLoginUser() 调用成功!\n\n")

        sb.append("返回结果:\n")
        if (loginUser != null) {
            sb.append("• 登录用户: $loginUser\n")
            sb.append("• 返回类型: String\n")
            sb.append("• 字符长度: ${loginUser.length}\n")
        } else {
            sb.append("• 登录用户: null\n")
            sb.append("• 返回类型: @Nullable String\n")
            sb.append("• 说明: 当前无登录用户\n")
        }

        return sb.toString()
    }

    /**
     * 构建错误结果文本
     */
    private fun buildErrorResultText(error: Exception): String {
        val sb = StringBuilder()

        sb.append("getLoginUser() 调用失败!\n\n")

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
}