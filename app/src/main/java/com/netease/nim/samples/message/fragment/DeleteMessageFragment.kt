package com.netease.nim.samples.message.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.common.getV2NIMMessageJson
import com.netease.nim.samples.databinding.FragmentDeleteMessageBinding
import com.netease.nim.samples.localconversation.common.V2LocalConversationSelectViewModel
import com.netease.nim.samples.localconversation.common.V2NIMLocalConversationSelectFragment
import com.netease.nim.samples.message.common.V2MessageSelectViewModel
import com.netease.nim.samples.message.common.V2NIMMessageSelectFragment
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.V2NIMError
import com.netease.nimlib.sdk.v2.message.V2NIMMessage
import com.netease.nimlib.sdk.v2.message.V2NIMMessageService

class DeleteMessageFragment : BaseMethodExecuteFragment<FragmentDeleteMessageBinding>() {

    /**
     * 选择会话的 ViewModel
     */
    private lateinit var selectConversationViewModel: V2LocalConversationSelectViewModel

    /**
     * 选择消息的 ViewModel
     */
    private lateinit var selectMessageViewModel: V2MessageSelectViewModel

    /**
     * 当前选择的消息
     */
    private var selectedMessage: V2NIMMessage? = null

    /**
     * 创建ViewBinding，由子类实现
     */
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDeleteMessageBinding {
        return FragmentDeleteMessageBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //选择会话
        binding.btnGetConversation.setOnClickListener {
            activityViewModel.addFragment(V2NIMLocalConversationSelectFragment.NAME)
        }

        //选择消息
        binding.btnSelectMessage.setOnClickListener {
            val conversationId = binding.etConversationId.text.toString()
            if (conversationId.isNotEmpty()) {
                activityViewModel.addFragment(V2NIMMessageSelectFragment.NAME, conversationId)
            } else {
                showToast("请先选择会话")
            }
        }

        selectConversationViewModel =
            getActivityViewModel(V2LocalConversationSelectViewModel::class.java)
        selectConversationViewModel.selectItemLiveData.observe(viewLifecycleOwner, {
            activityViewModel.popFragment()
            binding.etConversationId.setText(it.conversationId)
        })

        selectMessageViewModel = getActivityViewModel(V2MessageSelectViewModel::class.java)
        selectMessageViewModel.selectItemLiveData.observe(viewLifecycleOwner, { message ->
            activityViewModel.popFragment()
            selectedMessage = message
            binding.etMessageClientId.setText(message.messageClientId)
            
            val messageInfo = "消息类型: ${message.messageType}" +
                    if (!message.text.isNullOrEmpty()) ", 内容: ${message.text.take(20)}..." else ""
            binding.tvSelectedMessage.text = "已选择的消息: $messageInfo"
        })
    }

    /**
     * 发起请求
     */
    override fun onRequest() {
        // 获取要删除的消息
        val message = getMessageToDelete()
        if (message == null) {
            showToast("请选择要删除的消息")
            return
        }

        // 获取删除参数
        val serverExtension = binding.etServerExtension.text.toString().trim()
        val onlyDeleteLocal = binding.rbDeleteLocalOnly.isChecked

        activityViewModel.showLoadingDialog()
        NIMClient.getService(V2NIMMessageService::class.java).deleteMessage(
            message,
            serverExtension.ifEmpty { null },
            onlyDeleteLocal,
            { updateResult(true, null) },
            { error -> updateResult(false, error) }
        )
    }

    /**
     * 更新结果
     */
    private fun updateResult(success: Boolean, error: V2NIMError?) {
        activityViewModel.dismissLoadingDialog()
        
        if (error != null) {
            activityViewModel.refresh("删除消息失败: $error")
            return
        }

        if (success) {
            val deleteScope = if (binding.rbDeleteLocalOnly.isChecked) "仅本地" else "云端同步"
            val serverExt = binding.etServerExtension.text.toString().trim()
            val extInfo = if (serverExt.isNotEmpty()) "，扩展字段: $serverExt" else ""
            
            val resultText = buildString {
                append("删除消息成功!\n\n")
                append("删除范围: $deleteScope\n")
                append("消息ID: ${selectedMessage?.messageClientId}\n")
                if (extInfo.isNotEmpty()) {
                    append("扩展字段: $serverExt\n")
                }
                append("\n删除的消息信息:\n")
                selectedMessage?.let { 
                    append(getV2NIMMessageJson(it))
                }
            }
            activityViewModel.refresh(resultText)
        }
    }

    /**
     * 获取要删除的消息
     */
    private fun getMessageToDelete(): V2NIMMessage? {
        // 优先使用选择的消息
        if (selectedMessage != null) {
            return selectedMessage
        }

        // 如果没有选择消息，检查是否手动输入了消息ID
        val messageClientId = binding.etMessageClientId.text.toString().trim()
        if (messageClientId.isEmpty()) {
            return null
        }

        showToast("请通过选择消息的方式获取完整的消息对象")
        return null
    }
}