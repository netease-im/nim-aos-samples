package com.netease.nim.samples.login.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.databinding.FragmentGetKickedOfflineDetailBinding
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.auth.V2NIMLoginService
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMLoginStatus
import com.netease.nimlib.sdk.v2.auth.model.V2NIMKickedOfflineDetail
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMKickedOfflineReason
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMLoginClientType

class GetKickedOfflineDetailFragment : BaseMethodExecuteFragment<FragmentGetKickedOfflineDetailBinding>() {

    /**
     * 创建ViewBinding，由子类实现
     */
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentGetKickedOfflineDetailBinding {
        return FragmentGetKickedOfflineDetailBinding.inflate(inflater, container, false)
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
            clearKickedDetailDisplay()
            showToast("已清空被踢详情显示")
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
            val textColor = when (loginStatus) {
                V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGINED -> resources.getColor(android.R.color.holo_green_dark)
                V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGOUT -> resources.getColor(android.R.color.holo_red_dark)
                V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGINING -> resources.getColor(android.R.color.holo_blue_dark)
                else -> resources.getColor(android.R.color.holo_orange_dark)
            }
            
            binding.tvCurrentLoginStatus.setTextColor(textColor)
            binding.tvCurrentLoginUser.setTextColor(textColor)
            
            // 提示信息
            if (loginStatus == V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGINED) {
                binding.tvStatusTip.text = "当前已正常登录，如果之前被踢下线可查看详情"
                binding.tvStatusTip.setTextColor(resources.getColor(android.R.color.holo_green_dark))
            } else {
                binding.tvStatusTip.text = "当前未登录，可能是被踢下线或主动登出"
                binding.tvStatusTip.setTextColor(resources.getColor(android.R.color.holo_orange_dark))
            }
            
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
     * 清空被踢详情显示
     */
    private fun clearKickedDetailDisplay() {
        binding.tvKickedDetailStatus.text = "已清空显示，可重新获取被踢详情"
        binding.tvKickedDetailStatus.setTextColor(resources.getColor(android.R.color.holo_blue_dark))
        
        // 清空详情显示区域
        binding.tvKickedReason.text = "--"
        binding.tvKickedReasonDesc.text = "--"
        binding.tvKickedClientType.text = "--"
        binding.tvKickedCustomClientType.text = "--"
        
        // 重置颜色
        val defaultColor = resources.getColor(android.R.color.black)
        binding.tvKickedReason.setTextColor(defaultColor)
        binding.tvKickedReasonDesc.setTextColor(defaultColor)
        binding.tvKickedClientType.setTextColor(defaultColor)
        binding.tvKickedCustomClientType.setTextColor(defaultColor)
    }

    /**
     * 发起获取被踢下线详情请求
     */
    override fun onRequest() {
        try {
            val loginService = NIMClient.getService(V2NIMLoginService::class.java)
            
            binding.tvKickedDetailStatus.text = "正在获取被踢下线详情..."
            binding.tvKickedDetailStatus.setTextColor(resources.getColor(android.R.color.holo_blue_dark))
            
            // 执行 getKickedOfflineDetail() 方法
            val kickedDetail = loginService.kickedOfflineDetail
            
            // 获取登录状态用于结果分析
            val loginStatus = loginService.loginStatus
            val loginUser = loginService.loginUser
            
            updateResult(kickedDetail, loginStatus, loginUser, null)
            
        } catch (e: Exception) {
            updateResult(null, null, null, e)
        }
    }

    /**
     * 更新结果显示
     */
    private fun updateResult(
        kickedDetail: V2NIMKickedOfflineDetail?, 
        loginStatus: V2NIMLoginStatus?, 
        loginUser: String?, 
        error: Exception?
    ) {
        if (error != null) {
            val resultText = buildErrorResultText(error)
            activityViewModel.refresh(resultText)
            
            binding.tvKickedDetailStatus.text = "获取失败: ${error.message}"
            binding.tvKickedDetailStatus.setTextColor(resources.getColor(android.R.color.holo_red_dark))
            
            showToast("获取被踢下线详情失败: ${error.message}")
            return
        }

        val resultText = buildSuccessResultText(kickedDetail, loginStatus, loginUser)
        activityViewModel.refresh(resultText)
        
        // 更新详情显示
        updateKickedDetailDisplay(kickedDetail)
        
        // 更新登录状态显示
        updateCurrentLoginStatus()
        
        val toastMessage = if (kickedDetail == null) {
            "获取成功: 没有被踢下线记录"
        } else {
            "获取成功: 发现被踢下线记录"
        }
        showToast(toastMessage)
    }

    /**
     * 更新被踢详情显示
     */
    private fun updateKickedDetailDisplay(kickedDetail: V2NIMKickedOfflineDetail?) {
        if (kickedDetail == null) {
            binding.tvKickedDetailStatus.text = "无被踢下线记录"
            binding.tvKickedDetailStatus.setTextColor(resources.getColor(android.R.color.holo_green_dark))
            
            // 显示默认值
            binding.tvKickedReason.text = "无"
            binding.tvKickedReasonDesc.text = "无被踢记录"
            binding.tvKickedClientType.text = "无"
            binding.tvKickedCustomClientType.text = "无"
            
            val defaultColor = resources.getColor(android.R.color.holo_green_dark)
            binding.tvKickedReason.setTextColor(defaultColor)
            binding.tvKickedReasonDesc.setTextColor(defaultColor)
            binding.tvKickedClientType.setTextColor(defaultColor)
            binding.tvKickedCustomClientType.setTextColor(defaultColor)
            
        } else {
            binding.tvKickedDetailStatus.text = "发现被踢下线记录"
            binding.tvKickedDetailStatus.setTextColor(resources.getColor(android.R.color.holo_red_dark))
            
            // 显示详细信息
            binding.tvKickedReason.text = "${getReasonDisplayName(kickedDetail.reason)} (${kickedDetail.reason.value})"
            binding.tvKickedReasonDesc.text = kickedDetail.reasonDesc ?: "无描述"
            binding.tvKickedClientType.text = "${getClientTypeDisplayName(kickedDetail.clientType)} (${kickedDetail.clientType.value})"
            binding.tvKickedCustomClientType.text = kickedDetail.customClientType?.toString() ?: "无"
            
            // 根据被踢原因设置颜色
            val reasonColor = getReasonColor(kickedDetail.reason)
            binding.tvKickedReason.setTextColor(reasonColor)
            binding.tvKickedReasonDesc.setTextColor(reasonColor)
            binding.tvKickedClientType.setTextColor(reasonColor)
            binding.tvKickedCustomClientType.setTextColor(reasonColor)
        }
    }

    /**
     * 构建成功结果文本
     */
    private fun buildSuccessResultText(
        kickedDetail: V2NIMKickedOfflineDetail?, 
        loginStatus: V2NIMLoginStatus?, 
        loginUser: String?
    ): String {
        val sb = StringBuilder()

        sb.append("getKickedOfflineDetail() 调用成功!\n\n")

        sb.append("返回结果:\n")
        if (kickedDetail == null) {
            sb.append("• 被踢详情: null\n")
            sb.append("• 返回类型: V2NIMKickedOfflineDetail (可为null)\n")
            sb.append("• 说明: 没有被踢下线记录\n")
        } else {
            sb.append("• 被踢详情: 有记录\n")
            sb.append("• 返回类型: V2NIMKickedOfflineDetail\n")
            sb.append("• 说明: 发现被踢下线记录\n")
        }

        sb.append("\n当前状态:\n")
        loginStatus?.let {
            sb.append("• 登录状态: ${getLoginStatusDisplayName(it)}\n")
        }
        sb.append("• 登录用户: ${loginUser ?: "无"}\n")
        sb.append("• 调用时间: ${System.currentTimeMillis()}\n")
        sb.append("• 方法类型: 同步方法\n")

        sb.append("\n详情信息:\n")
        if (kickedDetail == null) {
            sb.append("• 无被踢下线记录\n")
        } else {
            sb.append("• 被踢原因: ${getReasonDisplayName(kickedDetail.reason)} (${kickedDetail.reason.value})\n")
            sb.append("• 原因描述: ${kickedDetail.reasonDesc ?: "无描述"}\n")
            sb.append("• 踢人端类型: ${getClientTypeDisplayName(kickedDetail.clientType)} (${kickedDetail.clientType.value})\n")
            sb.append("• 自定义客户端类型: ${kickedDetail.customClientType ?: "无"}\n")
        }

        sb.append("\n结果分析:\n")
        sb.append(analyzeResult(kickedDetail, loginStatus))

        sb.append("\n接口说明:\n")
        sb.append("• 返回null表示没有被踢下线记录\n")
        sb.append("• 返回对象表示有被踢下线的历史记录\n")
        sb.append("• 用于分析下线原因和实施对应处理策略\n")

        return sb.toString()
    }

    /**
     * 构建错误结果文本
     */
    private fun buildErrorResultText(error: Exception): String {
        val sb = StringBuilder()

        sb.append("getKickedOfflineDetail() 调用失败!\n\n")

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
    private fun analyzeResult(kickedDetail: V2NIMKickedOfflineDetail?, loginStatus: V2NIMLoginStatus?): String {
        return when {
            kickedDetail == null && loginStatus == V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGINED -> {
                "✅ 正常状态：当前已登录且没有被踢下线记录"
            }
            kickedDetail == null && loginStatus != V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGINED -> {
                "ℹ️ 当前未登录，但不是因为被踢下线"
            }
            kickedDetail != null && loginStatus == V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGINED -> {
                "⚠️ 之前被踢下线，但已重新登录成功"
            }
            kickedDetail != null && loginStatus != V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGINED -> {
                "🔴 当前未登录，原因是被踢下线：${getReasonDisplayName(kickedDetail.reason)}"
            }
            else -> {
                "❓ 状态未知，需要进一步分析"
            }
        }
    }

    /**
     * 获取被踢原因显示名称
     */
    private fun getReasonDisplayName(reason: V2NIMKickedOfflineReason): String {
        return when (reason) {
            V2NIMKickedOfflineReason.V2NIM_KICKED_OFFLINE_REASON_CLIENT_EXCLUSIVE -> "互斥登录被踢"
            V2NIMKickedOfflineReason.V2NIM_KICKED_OFFLINE_REASON_SERVER -> "服务器踢下线"
            V2NIMKickedOfflineReason.V2NIM_KICKED_OFFLINE_REASON_CLIENT -> "被其他端踢下线"
            else -> "未知原因"
        }
    }

    /**
     * 获取被踢原因颜色
     */
    private fun getReasonColor(reason: V2NIMKickedOfflineReason): Int {
        return when (reason) {
            V2NIMKickedOfflineReason.V2NIM_KICKED_OFFLINE_REASON_CLIENT_EXCLUSIVE -> 
                resources.getColor(android.R.color.holo_orange_dark)
            V2NIMKickedOfflineReason.V2NIM_KICKED_OFFLINE_REASON_SERVER -> 
                resources.getColor(android.R.color.holo_red_dark)
            V2NIMKickedOfflineReason.V2NIM_KICKED_OFFLINE_REASON_CLIENT -> 
                resources.getColor(android.R.color.holo_purple)
            else -> 
                resources.getColor(android.R.color.darker_gray)
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
}