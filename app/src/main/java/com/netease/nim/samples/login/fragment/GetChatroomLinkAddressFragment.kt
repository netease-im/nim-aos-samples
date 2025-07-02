package com.netease.nim.samples.login.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.databinding.FragmentGetChatroomLinkAddressBinding
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.auth.V2NIMLoginService
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMLoginStatus
import com.netease.nimlib.sdk.v2.V2NIMError
import com.netease.nimlib.sdk.v2.V2NIMFailureCallback
import com.netease.nimlib.sdk.v2.V2NIMSuccessCallback

class GetChatroomLinkAddressFragment : BaseMethodExecuteFragment<FragmentGetChatroomLinkAddressBinding>() {

    /**
     * 创建ViewBinding，由子类实现
     */
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentGetChatroomLinkAddressBinding {
        return FragmentGetChatroomLinkAddressBinding.inflate(inflater, container, false)
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

        // 清空输入按钮
        binding.btnClearInput.setOnClickListener {
            binding.etRoomId.setText("")
            showToast("已清空输入")
        }

        // 清空状态显示按钮
        binding.btnClearStatus.setOnClickListener {
            clearAddressDisplay()
            showToast("已清空聊天室地址显示")
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
                "✅ 已登录，可以获取聊天室Link地址"
            } else {
                "⚠️ 未登录，需要先登录才能获取聊天室Link地址"
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
     * 清空聊天室地址显示
     */
    private fun clearAddressDisplay() {
        binding.tvRequestStatus.text = "已清空显示，可重新获取聊天室Link地址"
        binding.tvRequestStatus.setTextColor(resources.getColor(android.R.color.holo_blue_dark))
        
        // 清空详情显示区域
        binding.tvRequestRoomId.text = "--"
        binding.tvAddressCount.text = "--"
        binding.tvOperationStatus.text = "--"
        binding.tvAddressList.text = "--"
        
        // 重置颜色
        val defaultColor = resources.getColor(android.R.color.black)
        binding.tvRequestRoomId.setTextColor(defaultColor)
        binding.tvAddressCount.setTextColor(defaultColor)
        binding.tvOperationStatus.setTextColor(defaultColor)
        binding.tvAddressList.setTextColor(defaultColor)
    }

    /**
     * 发起获取聊天室Link地址请求
     */
    override fun onRequest() {
        val roomId = binding.etRoomId.text.toString().trim()
        
        // 参数验证
        if (roomId.isEmpty()) {
            val errorMsg = "聊天室ID不能为空"
            binding.tvRequestStatus.text = "参数错误: $errorMsg"
            binding.tvRequestStatus.setTextColor(resources.getColor(android.R.color.holo_red_dark))
            showToast(errorMsg)
            return
        }
        
        try {
            val loginService = NIMClient.getService(V2NIMLoginService::class.java)
            
            // 检查登录状态
            val loginStatus = loginService.loginStatus
            if (loginStatus != V2NIMLoginStatus.V2NIM_LOGIN_STATUS_LOGINED) {
                val errorMsg = "请先登录再获取聊天室Link地址"
                binding.tvRequestStatus.text = "状态错误: $errorMsg"
                binding.tvRequestStatus.setTextColor(resources.getColor(android.R.color.holo_red_dark))
                showToast(errorMsg)
                return
            }
            
            // 更新请求状态
            binding.tvRequestStatus.text = "正在获取聊天室Link地址..."
            binding.tvRequestStatus.setTextColor(resources.getColor(android.R.color.holo_blue_dark))
            
            // 更新请求参数显示
            binding.tvRequestRoomId.text = roomId
            binding.tvRequestRoomId.setTextColor(resources.getColor(android.R.color.holo_blue_dark))
            
            // 执行 getChatroomLinkAddress() 方法
            loginService.getChatroomLinkAddress(
                roomId,
                object : V2NIMSuccessCallback<List<String>> {
                    override fun onSuccess(addressList: List<String>?) {
                        // 成功回调
                        updateResult(roomId, addressList, null)
                    }
                },
                object : V2NIMFailureCallback {
                    override fun onFailure(error: V2NIMError) {
                        // 失败回调
                        updateResult(roomId, null, error)
                    }
                }
            )
            
        } catch (e: Exception) {
            updateResult(roomId, null, e)
        }
    }

    /**
     * 更新结果显示
     */
    private fun updateResult(roomId: String, addressList: List<String>?, error: Any?) {
        if (error != null) {
            val errorMsg = when (error) {
                is V2NIMError -> "${error.code}: ${error.desc}"
                is Exception -> error.message ?: "未知异常"
                else -> error.toString()
            }
            
            val resultText = buildErrorResultText(roomId, error)
            activityViewModel.refresh(resultText)
            
            binding.tvRequestStatus.text = "获取失败: $errorMsg"
            binding.tvRequestStatus.setTextColor(resources.getColor(android.R.color.holo_red_dark))
            
            // 更新错误状态显示
            updateErrorDisplay(roomId, errorMsg)
            
            showToast("获取聊天室Link地址失败: $errorMsg")
            return
        }

        val resultText = buildSuccessResultText(roomId, addressList!!)
        activityViewModel.refresh(resultText)
        
        // 更新成功结果显示
        updateSuccessDisplay(roomId, addressList)
        
        // 更新登录状态显示
        updateCurrentLoginStatus()
        
        showToast("获取成功: 共${addressList.size}个地址")
    }

    /**
     * 更新成功结果显示
     */
    private fun updateSuccessDisplay(roomId: String, addressList: List<String>) {
        binding.tvRequestStatus.text = "获取聊天室Link地址成功"
        binding.tvRequestStatus.setTextColor(resources.getColor(android.R.color.holo_green_dark))
        
        // 显示请求参数
        binding.tvRequestRoomId.text = roomId
        binding.tvRequestRoomId.setTextColor(resources.getColor(android.R.color.holo_green_dark))
        
        // 显示地址数量
        binding.tvAddressCount.text = "${addressList.size}"
        binding.tvAddressCount.setTextColor(resources.getColor(android.R.color.holo_green_dark))
        
        // 显示操作状态
        binding.tvOperationStatus.text = "成功获取"
        binding.tvOperationStatus.setTextColor(resources.getColor(android.R.color.holo_green_dark))
        
        // 显示地址列表
        val addressText = if (addressList.isEmpty()) {
            "无可用地址"
        } else {
            buildString {
                addressList.forEachIndexed { index, address ->
                    append("${index + 1}. $address")
                    if (index < addressList.size - 1) append("\n")
                }
            }
        }
        binding.tvAddressList.text = addressText
        val addressColor = if (addressList.isEmpty()) {
            resources.getColor(android.R.color.holo_orange_dark)
        } else {
            resources.getColor(android.R.color.black)
        }
        binding.tvAddressList.setTextColor(addressColor)
    }

    /**
     * 更新错误结果显示
     */
    private fun updateErrorDisplay(roomId: String, errorMsg: String) {
        // 显示请求参数
        binding.tvRequestRoomId.text = roomId
        binding.tvRequestRoomId.setTextColor(resources.getColor(android.R.color.holo_red_dark))
        
        // 显示错误状态
        binding.tvAddressCount.text = "获取失败"
        binding.tvAddressCount.setTextColor(resources.getColor(android.R.color.holo_red_dark))
        
        binding.tvOperationStatus.text = "失败"
        binding.tvOperationStatus.setTextColor(resources.getColor(android.R.color.holo_red_dark))
        
        binding.tvAddressList.text = "错误: $errorMsg"
        binding.tvAddressList.setTextColor(resources.getColor(android.R.color.holo_red_dark))
    }

    /**
     * 构建成功结果文本
     */
    private fun buildSuccessResultText(roomId: String, addressList: List<String>): String {
        val sb = StringBuilder()

        sb.append("getChatroomLinkAddress() 调用成功!\n\n")

        sb.append("请求参数:\n")
        sb.append("• 聊天室ID: $roomId\n")
        sb.append("• 调用时间: ${System.currentTimeMillis()}\n")

        sb.append("\n返回结果:\n")
        sb.append("• 地址数量: ${addressList.size}\n")
        sb.append("• 返回类型: List<String>\n")
        sb.append("• 回调类型: V2NIMSuccessCallback\n")

        if (addressList.isNotEmpty()) {
            sb.append("\n地址列表详情:\n")
            addressList.forEachIndexed { index, address ->
                sb.append("${index + 1}. $address\n")
                sb.append("   长度: ${address.length} 字符\n")
                if (index < addressList.size - 1) sb.append("\n")
            }
        } else {
            sb.append("\n地址列表详情:\n")
            sb.append("• 当前无可用聊天室Link地址\n")
            sb.append("• 可能原因: 聊天室不存在或服务器配置问题\n")
        }

        sb.append("\n接口说明:\n")
        sb.append("• 异步回调方法，通过成功回调返回结果\n")
        sb.append("• 返回指定聊天室的所有可用Link地址\n")
        sb.append("• 地址用于聊天室连接和负载均衡\n")
        sb.append("• 需要在登录状态下调用\n")

        sb.append("\n使用建议:\n")
        sb.append("• 优先使用列表中的第一个地址\n")
        sb.append("• 可以实现地址轮询和故障转移\n")
        sb.append("• 定期更新地址列表以保证连接质量\n")

        return sb.toString()
    }

    /**
     * 构建错误结果文本
     */
    private fun buildErrorResultText(roomId: String, error: Any): String {
        val sb = StringBuilder()

        sb.append("getChatroomLinkAddress() 调用失败!\n\n")

        sb.append("请求参数:\n")
        sb.append("• 聊天室ID: $roomId\n")
        sb.append("• 调用时间: ${System.currentTimeMillis()}\n")

        sb.append("\n异常信息:\n")
        when (error) {
            is V2NIMError -> {
                sb.append("• 错误码: ${error.code}\n")
                sb.append("• 错误描述: ${error.desc}\n")
                sb.append("• 错误详情: ${error.detail}\n")
                sb.append("• 回调类型: V2NIMFailureCallback\n")
            }
            is Exception -> {
                sb.append("• 异常类型: ${error.javaClass.simpleName}\n")
                sb.append("• 异常消息: ${error.message ?: "未知异常"}\n")
                sb.append("• 异常位置: 方法调用过程\n")
            }
            else -> {
                sb.append("• 错误信息: $error\n")
                sb.append("• 错误类型: 未知错误\n")
            }
        }

        sb.append("\n可能原因:\n")
        sb.append("• 用户未登录或登录状态异常\n")
        sb.append("• 聊天室ID无效或不存在\n")
        sb.append("• 网络连接异常\n")
        sb.append("• 服务器内部错误\n")
        sb.append("• SDK未正确初始化\n")

        sb.append("\n解决建议:\n")
        sb.append("• 确认用户已成功登录\n")
        sb.append("• 检查聊天室ID是否正确\n")
        sb.append("• 确认网络连接状态\n")
        sb.append("• 重试请求或稍后再试\n")
        sb.append("• 查看详细错误日志\n")

        return sb.toString()
    }
}