package com.netease.nim.samples.login.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.databinding.FragmentGetLoginClientsBinding
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.auth.V2NIMLoginService
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMLoginStatus
import com.netease.nimlib.sdk.v2.auth.model.V2NIMLoginClient
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMLoginClientType
import java.text.SimpleDateFormat
import java.util.*

class GetLoginClientsFragment : BaseMethodExecuteFragment<FragmentGetLoginClientsBinding>() {

    private lateinit var clientsAdapter: LoginClientsAdapter

    /**
     * 创建ViewBinding，由子类实现
     */
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentGetLoginClientsBinding {
        return FragmentGetLoginClientsBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()
        initClickListeners()
        updateLoginStatusDisplay()
    }

    /**
     * 初始化RecyclerView
     */
    private fun initRecyclerView() {
        clientsAdapter = LoginClientsAdapter()
        binding.recyclerViewClients.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = clientsAdapter
        }
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

        // 清空列表按钮
        binding.btnClearList.setOnClickListener {
            clientsAdapter.clearData()
            binding.tvClientCount.text = "客户端数量: 0"
            showToast("已清空列表")
        }
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
                binding.tvLoginTip.text = "需要登录后才能获取其他端登录信息"
                binding.tvLoginTip.setTextColor(resources.getColor(android.R.color.holo_orange_dark))
            } else {
                binding.tvLoginTip.text = "当前已登录，可以获取其他端登录信息"
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
     * 发起请求 - 获取登录客户端列表
     */
    override fun onRequest() {
        try {
            val loginService = NIMClient.getService(V2NIMLoginService::class.java)
            
            // 执行 getLoginClients() 方法
            val loginClients = loginService.loginClients
            
            // 获取登录状态用于结果分析
            val loginStatus = loginService.loginStatus
            val loginUser = loginService.loginUser
            
            updateResult(loginClients, loginStatus, loginUser, null)
            
        } catch (e: Exception) {
            updateResult(null, null, null, e)
        }
    }

    /**
     * 更新结果显示
     */
    private fun updateResult(
        loginClients: List<V2NIMLoginClient>?, 
        loginStatus: V2NIMLoginStatus?, 
        loginUser: String?, 
        error: Exception?
    ) {
        if (error != null) {
            val resultText = buildErrorResultText(error)
            activityViewModel.refresh(resultText)
            showToast("获取登录客户端列表失败: ${error.message}")
            return
        }

        val resultText = buildSuccessResultText(loginClients, loginStatus, loginUser)
        activityViewModel.refresh(resultText)
        
        // 更新列表显示
        updateClientsListDisplay(loginClients)
        
        // 更新登录状态显示
        updateLoginStatusDisplay()
        
        val toastMessage = if (loginClients.isNullOrEmpty()) {
            "获取成功: 没有其他端登录"
        } else {
            "获取成功: 发现 ${loginClients.size} 个其他登录端"
        }
        showToast(toastMessage)
    }

    /**
     * 更新客户端列表显示
     */
    private fun updateClientsListDisplay(loginClients: List<V2NIMLoginClient>?) {
        if (loginClients.isNullOrEmpty()) {
            clientsAdapter.clearData()
            binding.tvClientCount.text = "客户端数量: 0"
            binding.tvEmptyTip.visibility = View.VISIBLE
            binding.recyclerViewClients.visibility = View.GONE
        } else {
            clientsAdapter.updateData(loginClients)
            binding.tvClientCount.text = "客户端数量: ${loginClients.size}"
            binding.tvEmptyTip.visibility = View.GONE
            binding.recyclerViewClients.visibility = View.VISIBLE
        }
    }

    /**
     * 构建成功结果文本
     */
    private fun buildSuccessResultText(
        loginClients: List<V2NIMLoginClient>?, 
        loginStatus: V2NIMLoginStatus?, 
        loginUser: String?
    ): String {
        val sb = StringBuilder()

        sb.append("getLoginClients() 调用成功!\n\n")

        sb.append("返回结果:\n")
        if (loginClients.isNullOrEmpty()) {
            sb.append("• 客户端列表: 空列表 ([])\n")
            sb.append("• 列表大小: 0\n")
            sb.append("• 返回类型: List<V2NIMLoginClient>\n")
            sb.append("• 说明: 没有其他端登录\n")
        } else {
            sb.append("• 客户端列表: 包含 ${loginClients.size} 个客户端\n")
            sb.append("• 列表大小: ${loginClients.size}\n")
            sb.append("• 返回类型: List<V2NIMLoginClient>\n")
            sb.append("• 说明: 发现其他登录端\n")
        }

        sb.append("\n当前状态:\n")
        loginStatus?.let {
            sb.append("• 登录状态: ${getLoginStatusDisplayName(it)}\n")
        }
        sb.append("• 登录用户: ${loginUser ?: "无"}\n")
        sb.append("• 调用时间: ${System.currentTimeMillis()}\n")
        sb.append("• 方法类型: 同步方法\n")

        sb.append("\n列表详情:\n")
        if (loginClients.isNullOrEmpty()) {
            sb.append("• 无其他登录端\n")
        } else {
            loginClients.forEachIndexed { index, client ->
                sb.append("• 客户端${index + 1}: ${getClientTypeDisplayName(client.type)}\n")
                sb.append("  - ID: ${client.clientId}\n")
                sb.append("  - OS: ${client.os}\n")
                sb.append("  - 登录时间: ${formatTimestamp(client.timestamp)}\n")
                if (!client.customTag.isNullOrEmpty()) {
                    sb.append("  - 自定义标签: ${client.customTag}\n")
                }
                sb.append("\n")
            }
        }

        sb.append("结果分析:\n")
        sb.append(analyzeResult(loginClients, loginStatus))

        sb.append("\n接口说明:\n")
        sb.append("• 返回的列表不包含当前登录端信息\n")
        sb.append("• 用于获取同一账号在其他设备的登录情况\n")
        sb.append("• 支持多端登录的账号管理\n")

        return sb.toString()
    }

    /**
     * 构建错误结果文本
     */
    private fun buildErrorResultText(error: Exception): String {
        val sb = StringBuilder()

        sb.append("getLoginClients() 调用失败!\n\n")

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
    private fun analyzeResult(loginClients: List<V2NIMLoginClient>?, loginStatus: V2NIMLoginStatus?): String {
        return when {
            loginStatus != V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGINED -> {
                "🔴 当前未登录状态，无法获取其他端登录信息"
            }
            loginClients.isNullOrEmpty() -> {
                "ℹ️ 正常状态：当前只有本端登录，没有其他端"
            }
            else -> {
                "✅ 正常状态：发现 ${loginClients.size} 个其他登录端"
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
            val sdf = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
            sdf.format(Date(timestamp))
        } else {
            "未知时间"
        }
    }
}