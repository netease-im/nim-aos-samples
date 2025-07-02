package com.netease.nim.samples.message.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.common.getV2NIMMessageListJson
import com.netease.nim.samples.databinding.FragmentGetMessageListByRefersBinding
import com.netease.nim.samples.localconversation.common.V2LocalConversationSelectViewModel
import com.netease.nim.samples.localconversation.common.V2NIMLocalConversationSelectFragment
import com.netease.nim.samples.message.common.V2MessageMultiSelectViewModel
import com.netease.nim.samples.message.common.V2NIMMessageMultiSelectFragment
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.V2NIMError
import com.netease.nimlib.sdk.v2.conversation.enums.V2NIMConversationType
import com.netease.nimlib.sdk.v2.message.V2NIMMessage
import com.netease.nimlib.sdk.v2.message.V2NIMMessageRefer
import com.netease.nimlib.sdk.v2.message.V2NIMMessageReferBuilder
import com.netease.nimlib.sdk.v2.message.V2NIMMessageService

class GetMessageListByRefersFragment : BaseMethodExecuteFragment<FragmentGetMessageListByRefersBinding>() {

    /**
     * 选择会话的 ViewModel
     */
    private lateinit var selectConversationViewModel: V2LocalConversationSelectViewModel

    /**
     * 选择消息的 ViewModel
     */
    private lateinit var selectMessageMultiViewModel: V2MessageMultiSelectViewModel

    /**
     * MessageRefer列表
     */
    private val messageReferList = mutableListOf<V2NIMMessageRefer>()

    /**
     * 创建ViewBinding，由子类实现
     */
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentGetMessageListByRefersBinding {
        return FragmentGetMessageListByRefersBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initClickListeners()
        initViewModels()
        updateUI()
    }

    private fun initClickListeners() {
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

        //手动添加MessageRefer
        binding.btnAddManualRefer.setOnClickListener {
            toggleManualReferLayout(true)
        }

        //清空MessageRefer列表
        binding.btnClearRefers.setOnClickListener {
            messageReferList.clear()
            updateUI()
            showToast("已清空MessageRefer列表")
        }

        //确认添加手动输入的MessageRefer
        binding.btnConfirmAddRefer.setOnClickListener {
            addManualMessageRefer()
        }

        //取消手动添加
        binding.btnCancelAddRefer.setOnClickListener {
            toggleManualReferLayout(false)
            clearManualInputs()
        }
    }

    private fun initViewModels() {
        selectConversationViewModel =
            getActivityViewModel(V2LocalConversationSelectViewModel::class.java)
        selectConversationViewModel.selectItemLiveData.observe(viewLifecycleOwner, {
            activityViewModel.popFragment()
            binding.etConversationId.setText(it.conversationId)
        })

        selectMessageMultiViewModel = getActivityViewModel(V2MessageMultiSelectViewModel::class.java)
        selectMessageMultiViewModel.selectListLiveData.observe(viewLifecycleOwner, { messageList ->
            activityViewModel.popFragment()

            // 将选中的消息转换为MessageRefer并添加到列表
            messageList.forEach { message ->
                val messageRefer = createMessageReferFromMessage(message)
                if (!messageReferList.any { it.messageClientId == messageRefer.messageClientId }) {
                    messageReferList.add(messageRefer)
                }
            }
            updateUI()
            showToast("已添加${messageList.size}个MessageRefer")
        })
    }

    /**
     * 发起请求
     */
    override fun onRequest() {
        if (messageReferList.isEmpty()) {
            showToast("请添加MessageRefer列表")
            return
        }

        activityViewModel.showLoadingDialog()
        NIMClient.getService(V2NIMMessageService::class.java).getMessageListByRefers(
            messageReferList,
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
     * 从消息创建MessageRefer
     */
    private fun createMessageReferFromMessage(message: V2NIMMessage): V2NIMMessageRefer {
        return V2NIMMessageReferBuilder.builder()
            .withSenderId(message.senderId)
            .withReceiverId(message.receiverId)
            .withMessageClientId(message.messageClientId)
            .withMessageServerId(message.messageServerId)
            .withConversationType(message.conversationType)
            .withConversationId(message.conversationId)
            .withCreateTime(message.createTime)
            .build()
    }

    /**
     * 手动添加MessageRefer
     */
    private fun addManualMessageRefer() {
        val senderId = binding.etSenderId.text.toString().trim()
        val receiverId = binding.etReceiverId.text.toString().trim()
        val messageClientId = binding.etMessageClientId.text.toString().trim()
        val messageServerId = binding.etMessageServerId.text.toString().trim()
        val createTimeStr = binding.etCreateTime.text.toString().trim()

        // 基本参数验证
        if (senderId.isEmpty() || receiverId.isEmpty() || messageClientId.isEmpty()) {
            showToast("发送者ID、接收者ID和客户端消息ID不能为空")
            return
        }

        val createTime = try {
            if (createTimeStr.isEmpty()) 0L else createTimeStr.toLong()
        } catch (e: NumberFormatException) {
            showToast("创建时间格式错误")
            return
        }

        // 获取会话ID和会话类型
        val conversationId = binding.etConversationId.text.toString().trim()
        if (conversationId.isEmpty()) {
            showToast("请先选择会话")
            return
        }

        // 根据会话ID判断会话类型（简单的判断逻辑）
        val conversationType = if (conversationId.contains("team_")) {
            V2NIMConversationType.V2NIM_CONVERSATION_TYPE_TEAM
        } else {
            V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P
        }

        try {
            val messageRefer = V2NIMMessageReferBuilder.builder()
                .withSenderId(senderId)
                .withReceiverId(receiverId)
                .withMessageClientId(messageClientId)
                .withMessageServerId(messageServerId.ifEmpty { null })
                .withConversationType(conversationType)
                .withConversationId(conversationId)
                .withCreateTime(createTime)
                .build()

            // 检查是否已存在相同的MessageRefer
            if (messageReferList.any { it.messageClientId == messageClientId }) {
                showToast("该MessageRefer已存在")
                return
            }

            messageReferList.add(messageRefer)
            toggleManualReferLayout(false)
            clearManualInputs()
            updateUI()
            showToast("已添加MessageRefer")
        } catch (e: Exception) {
            showToast("创建MessageRefer失败: ${e.message}")
        }
    }

    /**
     * 切换手动添加MessageRefer布局的显示状态
     */
    private fun toggleManualReferLayout(show: Boolean) {
        binding.layoutManualRefer.visibility = if (show) View.VISIBLE else View.GONE
    }

    /**
     * 清空手动输入的内容
     */
    private fun clearManualInputs() {
        binding.etSenderId.setText("")
        binding.etReceiverId.setText("")
        binding.etMessageClientId.setText("")
        binding.etMessageServerId.setText("")
        binding.etCreateTime.setText("")
    }

    /**
     * 更新UI显示
     */
    private fun updateUI() {
        binding.tvSelectedRefersCount.text = "已选择的MessageRefer: ${messageReferList.size}个"

        if (messageReferList.isEmpty()) {
            binding.tvMessageRefersDetail.text = "暂无MessageRefer"
        } else {
            val detailText = messageReferList.mapIndexed { index, refer ->
                "${index + 1}. clientId: ${refer.messageClientId}\n" +
                "   serverId: ${refer.messageServerId ?: "null"}\n" +
                "   senderId: ${refer.senderId}\n" +
                "   receiverId: ${refer.receiverId}\n" +
                "   conversationId: ${refer.conversationId}\n" +
                "   createTime: ${refer.createTime}\n"
            }.joinToString("\n")
            binding.tvMessageRefersDetail.text = detailText
        }
    }
}