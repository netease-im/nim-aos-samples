package com.netease.nim.samples.message.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.common.getV2NIMMessageListJson
import com.netease.nim.samples.databinding.FragmentGetMessageListByIdsBinding
import com.netease.nim.samples.localconversation.common.V2LocalConversationSelectViewModel
import com.netease.nim.samples.localconversation.common.V2NIMLocalConversationSelectFragment
import com.netease.nim.samples.message.common.V2MessageMultiSelectViewModel
import com.netease.nim.samples.message.common.V2NIMMessageMultiSelectFragment
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.V2NIMError
import com.netease.nimlib.sdk.v2.message.V2NIMMessage
import com.netease.nimlib.sdk.v2.message.V2NIMMessageService

class GetMessageListByIdsFragment : BaseMethodExecuteFragment<FragmentGetMessageListByIdsBinding>() {

    /**
     * 选择会话的 ViewModel
     */
    private lateinit var selectConversationViewModel: V2LocalConversationSelectViewModel

    /**
     * 选择消息的 ViewModel
     */
    private lateinit var selectMessageMultiViewModel: V2MessageMultiSelectViewModel

    /**
     * 创建ViewBinding，由子类实现
     */
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentGetMessageListByIdsBinding {
        return FragmentGetMessageListByIdsBinding.inflate(inflater, container, false)
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

        selectConversationViewModel =
            getActivityViewModel(V2LocalConversationSelectViewModel::class.java)
        selectConversationViewModel.selectItemLiveData.observe(viewLifecycleOwner, {
            activityViewModel.popFragment()
            binding.etConversationId.setText(it.conversationId)
        })

        selectMessageMultiViewModel = getActivityViewModel(V2MessageMultiSelectViewModel::class.java)
        selectMessageMultiViewModel.selectListLiveData.observe(viewLifecycleOwner, { messageList ->
            activityViewModel.popFragment()
            val messageIds = messageList.map { it.messageClientId }
            val idsText = messageIds.joinToString(",")
            binding.etMessageClientIds.setText(idsText)
            binding.tvSelectedMessageIds.text = "已选择的消息ID: ${messageIds.size}个"
        })
    }

    /**
     * 发起请求
     */
    override fun onRequest() {
        val messageClientIds = getMessageClientIdList()
        if (messageClientIds.isEmpty()) {
            showToast("请输入消息客户端ID列表")
            return
        }

        activityViewModel.showLoadingDialog()
        NIMClient.getService(V2NIMMessageService::class.java).getMessageListByIds(
            messageClientIds,
            { messageList -> updateResult(messageList, null) },
            { error -> updateResult(null, error) }
        )
    }

    /**
     * 更新结果
     */
    private fun updateResult(messageList: List<V2NIMMessage>?, error: V2NIMError?) {
        activityViewModel.dismissLoadingDialog()
        if (error != null) {
            activityViewModel.refresh("查询消息失败: $error")
            return
        }

        val resultText = if (messageList.isNullOrEmpty()) {
            "查询消息成功: 未找到匹配的消息"
        } else {
            "查询消息成功: 找到${messageList.size}条消息\n${getV2NIMMessageListJson(messageList)}"
        }
        activityViewModel.refresh(resultText)
    }

    /**
     * 获取消息客户端ID列表
     */
    private fun getMessageClientIdList(): List<String> {
        val idsText = binding.etMessageClientIds.text.toString().trim()
        if (idsText.isEmpty()) {
            return emptyList()
        }

        return idsText.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }
}