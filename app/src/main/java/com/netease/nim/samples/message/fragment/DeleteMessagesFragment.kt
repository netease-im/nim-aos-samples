package com.netease.nim.samples.message.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.common.getV2NIMMessageListJson
import com.netease.nim.samples.databinding.FragmentDeleteMessagesBinding
import com.netease.nim.samples.localconversation.common.V2LocalConversationSelectViewModel
import com.netease.nim.samples.localconversation.common.V2NIMLocalConversationSelectFragment
import com.netease.nim.samples.message.common.V2MessageMultiSelectViewModel
import com.netease.nim.samples.message.common.V2NIMMessageMultiSelectFragment
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.V2NIMError
import com.netease.nimlib.sdk.v2.message.V2NIMMessage
import com.netease.nimlib.sdk.v2.message.V2NIMMessageService

class DeleteMessagesFragment : BaseMethodExecuteFragment<FragmentDeleteMessagesBinding>() {

    /**
     * 选择会话的 ViewModel
     */
    private lateinit var selectConversationViewModel: V2LocalConversationSelectViewModel

    /**
     * 选择消息的 ViewModel
     */
    private lateinit var selectMessageMultiViewModel: V2MessageMultiSelectViewModel

    /**
     * 当前选择的消息列表
     */
    private var selectedMessages: List<V2NIMMessage> = emptyList()

    /**
     * 创建ViewBinding，由子类实现
     */
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDeleteMessagesBinding {
        return FragmentDeleteMessagesBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //选择会话
        binding.btnGetConversation.setOnClickListener {
            activityViewModel.addFragment(V2NIMLocalConversationSelectFragment.NAME)
        }

        //选择消息
        binding.btnSelectMessages.setOnClickListener {
            val conversationId = binding.etConversationId.text.toString()
            if (conversationId.isNotEmpty()) {
                activityViewModel.addFragment(V2NIMMessageMultiSelectFragment.NAME, conversationId)
            } else {
                showToast("请先选择会话")
            }
        }

        //清空选择
        binding.btnClearSelection.setOnClickListener {
            clearSelection()
        }

        selectConversationViewModel =
            getActivityViewModel(V2LocalConversationSelectViewModel::class.java)
        selectConversationViewModel.selectItemLiveData.observe(viewLifecycleOwner, {
            activityViewModel.popFragment()
            binding.etConversationId.setText(it.conversationId)
            // 切换会话时清空已选择的消息
            clearSelection()
        })

        selectMessageMultiViewModel = getActivityViewModel(V2MessageMultiSelectViewModel::class.java)
        selectMessageMultiViewModel.selectListLiveData.observe(viewLifecycleOwner, { messageList ->
            activityViewModel.popFragment()
            selectedMessages = messageList
            updateSelectedMessagesDisplay()
        })
    }

    /**
     * 清空选择
     */
    private fun clearSelection() {
        selectedMessages = emptyList()
        selectMessageMultiViewModel.clearSelection()
        updateSelectedMessagesDisplay()
    }

    /**
     * 更新选择的消息显示
     */
    private fun updateSelectedMessagesDisplay() {
        val count = selectedMessages.size

        if (count == 0) {
            binding.tvSelectedMessages.text = "已选择的消息: 0条"
        } else {
            // 显示消息数量和clientId列表
            val clientIds = selectedMessages.map { it.messageClientId }
            val clientIdsText = if (clientIds.size <= 10) {
                // 如果消息数量少于等于10条，显示完整列表
                clientIds.joinToString("\n• ", "• ")
            } else {
                // 如果消息数量超过10条，显示前5条和后5条
                val first5 = clientIds.take(5)
                val last5 = clientIds.takeLast(5)
                "${first5.joinToString("\n• ", "• ")}\n• ... (省略${clientIds.size - 10}条) ...\n• ${last5.joinToString("\n• ")}"
            }

            binding.tvSelectedMessages.text = "已选择的消息: ${count}条\n消息ClientId列表:\n$clientIdsText"
        }
        
        // 更新数量警告颜色
        if (count > 50) {
            binding.tvMessageCountWarning.text = "⚠️ 错误: 选择了${count}条消息，超过50条限制！"
            binding.tvMessageCountWarning.setTextColor(resources.getColor(android.R.color.holo_red_dark))
        } else if (count > 40) {
            binding.tvMessageCountWarning.text = "⚠️ 警告: 已选择${count}条消息，接近50条限制"
            binding.tvMessageCountWarning.setTextColor(resources.getColor(android.R.color.holo_orange_dark))
        } else {
            binding.tvMessageCountWarning.text = "注意: 每次最多删除50条消息 (当前: ${count}条)"
            binding.tvMessageCountWarning.setTextColor(resources.getColor(android.R.color.holo_blue_dark))
        }
    }

    /**
     * 发起请求
     */
    override fun onRequest() {
        // 验证选择的消息
        if (selectedMessages.isEmpty()) {
            showToast("请选择要删除的消息")
            return
        }

        if (selectedMessages.size > 50) {
            showToast("选择的消息超过50条限制，请重新选择")
            return
        }

        // 验证消息是否属于同一会话
        val conversationIds = selectedMessages.map { it.conversationId }.distinct()
        if (conversationIds.size > 1) {
            showToast("选择的消息不属于同一个会话，请重新选择")
            return
        }

        // 获取删除参数
        val serverExtension = binding.etServerExtension.text.toString().trim()
        val onlyDeleteLocal = binding.rbDeleteLocalOnly.isChecked

        activityViewModel.showLoadingDialog()
        NIMClient.getService(V2NIMMessageService::class.java).deleteMessages(
            selectedMessages,
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
            activityViewModel.refresh("批量删除消息失败: $error")
            return
        }

        if (success) {
            val deleteScope = if (binding.rbDeleteLocalOnly.isChecked) "仅本地" else "云端同步"
            val serverExt = binding.etServerExtension.text.toString().trim()
            val extInfo = if (serverExt.isNotEmpty()) "，扩展字段: $serverExt" else ""
            
            val resultText = buildString {
                append("批量删除消息成功!\n\n")
                append("删除数量: ${selectedMessages.size}条\n")
                append("删除范围: $deleteScope\n")
                append("会话ID: ${selectedMessages.firstOrNull()?.conversationId ?: "未知"}\n")
                if (extInfo.isNotEmpty()) {
                    append("扩展字段: $serverExt\n")
                }
                append("\n删除的消息列表:\n")
                append(getV2NIMMessageListJson(selectedMessages))
            }
            activityViewModel.refresh(resultText)
            
            // 删除成功后清空选择
            clearSelection()
        }
    }
}