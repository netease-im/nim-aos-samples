package com.netease.nim.samples.login.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.databinding.FragmentKickOfflineBinding
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.V2NIMError
import com.netease.nimlib.sdk.v2.auth.V2NIMLoginService
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMLoginStatus
import com.netease.nimlib.sdk.v2.auth.model.V2NIMLoginClient
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMLoginClientType
import java.text.SimpleDateFormat
import java.util.*

class KickOfflineFragment : BaseMethodExecuteFragment<FragmentKickOfflineBinding>() {

    private lateinit var clientsAdapter: KickOfflineClientsAdapter
    private var selectedClient: V2NIMLoginClient? = null

    /**
     * 创建ViewBinding，由子类实现
     */
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentKickOfflineBinding {
        return FragmentKickOfflineBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()
        initClickListeners()
        updateLoginStatusAndLoadClients()
    }

    /**
     * 初始化RecyclerView
     */
    private fun initRecyclerView() {
        clientsAdapter = KickOfflineClientsAdapter { client ->
            selectedClient = client
            updateSelectedClientDisplay(client)
            showToast("已选择: ${getClientTypeDisplayName(client.type)}")
        }
        
        binding.recyclerViewClients.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = clientsAdapter
        }
    }

    /**
     * 初始化点击监听
     */
    private fun initClickListeners() {
        // 刷新登录状态和客户端列表
        binding.btnRefreshClients.setOnClickListener {
            updateLoginStatusAndLoadClients()
            showToast("已刷新客户端列表")
        }

        // 清空选择
        binding.btnClearSelection.setOnClickListener {
            selectedClient = null
            clientsAdapter.clearSelection()
            updateSelectedClientDisplay(null)
            showToast("已清空选择")
        }
    }

    /**
     * 更新登录状态并加载客户端列表
     */
    private fun updateLoginStatusAndLoadClients() {
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

            // 获取并显示客户端列表
            if (loginStatus == V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGINED) {
                val loginClients = loginService.loginClients
                updateClientsListDisplay(loginClients)
                
                binding.tvLoginTip.text = "当前已登录，可以踢掉其他端"
                binding.tvLoginTip.setTextColor(resources.getColor(android.R.color.holo_green_dark))
            } else {
                clientsAdapter.clearData()
                binding.tvClientCount.text = "客户端数量: 0"
                binding.tvEmptyTip.visibility = View.VISIBLE
                binding.recyclerViewClients.visibility = View.GONE
                
                binding.tvLoginTip.text = "需要登录后才能查看和踢掉其他端"
                binding.tvLoginTip.setTextColor(resources.getColor(android.R.color.holo_orange_dark))
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
     * 更新客户端列表显示
     */
    private fun updateClientsListDisplay(loginClients: List<V2NIMLoginClient>?) {
        if (loginClients.isNullOrEmpty()) {
            clientsAdapter.clearData()
            binding.tvClientCount.text = "客户端数量: 0"
            binding.tvEmptyTip.visibility = View.VISIBLE
            binding.tvEmptyTip.text = "没有其他端登录，无需踢掉操作"
            binding.recyclerViewClients.visibility = View.GONE
        } else {
            clientsAdapter.updateData(loginClients)
            binding.tvClientCount.text = "客户端数量: ${loginClients.size}"
            binding.tvEmptyTip.visibility = View.GONE
            binding.recyclerViewClients.visibility = View.VISIBLE
        }
    }

    /**
     * 更新选中的客户端显示
     */
    private fun updateSelectedClientDisplay(client: V2NIMLoginClient?) {
        if (client == null) {
            binding.tvSelectedClient.text = "未选择"
            binding.tvSelectedClientDetails.text = "请从上方列表中选择要踢掉的客户端"
            binding.tvKickWarning.text = "选择客户端后，点击请求按钮执行踢掉操作"
            binding.tvKickWarning.setTextColor(resources.getColor(android.R.color.holo_blue_dark))
        } else {
            binding.tvSelectedClient.text = "${getClientTypeDisplayName(client.type)} (${client.clientId})"
            
            val details = buildString {
                append("• 客户端类型: ${getClientTypeDisplayName(client.type)}\n")
                append("• 操作系统: ${client.os ?: "未知"}\n")
                append("• 客户端ID: ${client.clientId ?: "未知"}\n")
                append("• 登录时间: ${formatTimestamp(client.timestamp)}\n")
                if (!client.customTag.isNullOrEmpty()) {
                    append("• 自定义标签: ${client.customTag}\n")
                }
                append("• 自定义类型: ${client.customClientType}")
            }
            binding.tvSelectedClientDetails.text = details
            
            binding.tvKickWarning.text = "⚠️ 警告：踢掉该客户端后，对方将立即下线并需要重新登录"
            binding.tvKickWarning.setTextColor(resources.getColor(android.R.color.holo_red_dark))
        }
    }

    /**
     * 发起踢掉客户端请求
     */
    override fun onRequest() {
        val loginService = NIMClient.getService(V2NIMLoginService::class.java)
        
        // 检查当前登录状态
        if (loginService.loginStatus != V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGINED) {
            showToast("当前未登录，无法执行踢掉操作")
            activityViewModel.refresh("踢掉客户端失败: 当前未登录状态")
            return
        }

        // 检查是否选择了客户端
        val clientToKick = selectedClient
        if (clientToKick == null) {
            showToast("请先选择要踢掉的客户端")
            activityViewModel.refresh("踢掉客户端失败: 未选择目标客户端")
            return
        }
        
        activityViewModel.showLoadingDialog()
        
        loginService.kickOffline(
            clientToKick,
            { 
                // 踢掉成功
                handleKickOfflineSuccess(clientToKick)
            },
            { error -> 
                // 踢掉失败
                handleKickOfflineFailure(clientToKick, error)
            }
        )
    }

    /**
     * 处理踢掉成功
     */
    private fun handleKickOfflineSuccess(kickedClient: V2NIMLoginClient) {
        activityViewModel.dismissLoadingDialog()

        val resultText = buildSuccessResultText(kickedClient)
        activityViewModel.refresh(resultText)

        showToast("踢掉客户端成功")
        
        // 刷新客户端列表
        updateLoginStatusAndLoadClients()
        
        // 清空选择
        selectedClient = null
        clientsAdapter.clearSelection()
        updateSelectedClientDisplay(null)
    }

    /**
     * 处理踢掉失败
     */
    private fun handleKickOfflineFailure(targetClient: V2NIMLoginClient, error: V2NIMError) {
        activityViewModel.dismissLoadingDialog()
        
        val resultText = buildFailureResultText(targetClient, error)
        activityViewModel.refresh(resultText)
        
        showToast("踢掉客户端失败: ${error.desc}")
    }

    /**
     * 构建成功结果文本
     */
    private fun buildSuccessResultText(kickedClient: V2NIMLoginClient): String {
        val sb = StringBuilder()

        sb.append("kickOffline() 调用成功!\n\n")

        sb.append("踢掉详情:\n")
        sb.append("• 目标客户端: ${getClientTypeDisplayName(kickedClient.type)}\n")
        sb.append("• 客户端ID: ${kickedClient.clientId}\n")
        sb.append("• 操作系统: ${kickedClient.os}\n")
        sb.append("• 登录时间: ${formatTimestamp(kickedClient.timestamp)}\n")
        sb.append("• 执行时间: ${System.currentTimeMillis()}\n")
        sb.append("• 操作结果: 成功踢掉\n\n")

        sb.append("操作影响:\n")
        sb.append("• 目标客户端将立即下线\n")
        sb.append("• 对方会收到被踢掉的通知\n")
        sb.append("• 对方需要重新登录才能继续使用\n")
        sb.append("• 当前端继续保持在线状态\n\n")

        sb.append("接口说明:\n")
        sb.append("• 方法名: kickOffline(V2NIMLoginClient, success, failure)\n")
        sb.append("• 参数类型: V2NIMLoginClient 对象\n")
        sb.append("• 回调类型: V2NIMSuccessCallback<Void>\n")
        sb.append("• 功能: 主动踢掉指定的登录客户端\n")

        return sb.toString()
    }

    /**
     * 构建失败结果文本
     */
    private fun buildFailureResultText(targetClient: V2NIMLoginClient, error: V2NIMError): String {
        val sb = StringBuilder()
        
        sb.append("kickOffline() 调用失败!\n\n")
        
        sb.append("目标客户端:\n")
        sb.append("• 客户端类型: ${getClientTypeDisplayName(targetClient.type)}\n")
        sb.append("• 客户端ID: ${targetClient.clientId}\n")
        sb.append("• 操作系统: ${targetClient.os}\n\n")
        
        sb.append("错误信息:\n")
        sb.append("• 错误码: ${error.code}\n")
        sb.append("• 错误描述: ${error.desc}\n")
        sb.append("• 错误详情: ${error.detail ?: "无"}\n")
        sb.append("• 执行时间: ${System.currentTimeMillis()}\n\n")
        
        sb.append("可能原因:\n")
        sb.append("• 目标客户端已经下线\n")
        sb.append("• 网络连接异常\n")
        sb.append("• 服务器响应超时\n")
        sb.append("• 权限不足或客户端状态异常\n")
        sb.append("• 目标客户端信息已过期\n\n")
        
        sb.append("解决建议:\n")
        sb.append("• 刷新客户端列表获取最新状态\n")
        sb.append("• 检查网络连接是否正常\n")
        sb.append("• 确认目标客户端是否仍在线\n")
        sb.append("• 稍后重试操作\n")
        
        return sb.toString()
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
     * 格式化时间戳
     */
    private fun formatTimestamp(timestamp: Long): String {
        return if (timestamp > 0) {
            val sdf = SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault())
            sdf.format(Date(timestamp))
        } else {
            "未知时间"
        }
    }
}