package com.netease.nim.samples.localconversation.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.databinding.FragmentCreateConversationBinding
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.V2NIMError
import com.netease.nimlib.sdk.v2.conversation.V2NIMLocalConversationService
import com.netease.nimlib.sdk.v2.conversation.enums.V2NIMConversationType
import com.netease.nimlib.sdk.v2.conversation.model.V2NIMLocalConversation
import com.netease.nimlib.sdk.v2.conversation.model.V2NIMLastMessage
import com.netease.nimlib.sdk.v2.utils.V2NIMConversationIdUtil

class CreateConversationFragment : BaseMethodExecuteFragment<FragmentCreateConversationBinding>() {

    /**
     * 当前选择的会话类型
     */
    private var currentConversationType = V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P

    /**
     * 创建ViewBinding，由子类实现
     */
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCreateConversationBinding {
        return FragmentCreateConversationBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initClickListeners()
        updatePreview()
    }

    private fun initViews() {
        // 默认选中单聊
        binding.rbP2p.isChecked = true
        updateTargetLabel()
    }

    private fun initClickListeners() {
        // 会话类型选择监听
        binding.rgConversationType.setOnCheckedChangeListener { _, checkedId ->
            currentConversationType = when (checkedId) {
                binding.rbP2p.id -> V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P
                binding.rbTeam.id -> V2NIMConversationType.V2NIM_CONVERSATION_TYPE_TEAM
                binding.rbSuperTeam.id -> V2NIMConversationType.V2NIM_CONVERSATION_TYPE_SUPER_TEAM
                else -> V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P
            }
            updateTargetLabel()
            updatePreview()
        }

        // 目标ID输入监听
        binding.edtTargetId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updatePreview()
            }
        })

        // 快速输入按钮
        binding.btnQuickP2p.setOnClickListener {
            binding.rbP2p.isChecked = true
            binding.edtTargetId.setText("user123")
            showToast("已填入单聊示例")
        }

        binding.btnQuickTeam.setOnClickListener {
            binding.rbTeam.isChecked = true
            binding.edtTargetId.setText("team456")
            showToast("已填入群聊示例")
        }

        binding.btnQuickSuperTeam.setOnClickListener {
            binding.rbSuperTeam.isChecked = true
            binding.edtTargetId.setText("superteam789")
            showToast("已填入超大群示例")
        }

        binding.btnClear.setOnClickListener {
            binding.edtTargetId.setText("")
            showToast("已清空目标ID")
        }
    }

    /**
     * 更新目标标签
     */
    private fun updateTargetLabel() {
        val labelText = when (currentConversationType) {
            V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P -> "用户账号:"
            V2NIMConversationType.V2NIM_CONVERSATION_TYPE_TEAM -> "群组ID:"
            V2NIMConversationType.V2NIM_CONVERSATION_TYPE_SUPER_TEAM -> "超大群ID:"
            else -> "目标ID:"
        }
        binding.tvTargetLabel.text = labelText

        val hintText = when (currentConversationType) {
            V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P -> "请输入目标用户账号"
            V2NIMConversationType.V2NIM_CONVERSATION_TYPE_TEAM -> "请输入群组ID"
            V2NIMConversationType.V2NIM_CONVERSATION_TYPE_SUPER_TEAM -> "请输入超大群ID"
            else -> "请输入目标ID"
        }
        binding.edtTargetId.hint = hintText
    }

    /**
     * 更新会话ID预览
     */
    private fun updatePreview() {
        val targetId = binding.edtTargetId.text.toString().trim()

        if (targetId.isEmpty()) {
            binding.tvConversationIdPreview.text = "请先输入目标ID"
            return
        }

        val conversationId = generateConversationId(targetId)
        if (conversationId != null) {
            binding.tvConversationIdPreview.text = conversationId
        } else {
            binding.tvConversationIdPreview.text = "无法生成会话ID（请检查登录状态）"
        }
    }

    /**
     * 生成会话ID
     */
    private fun generateConversationId(targetId: String): String? {
        return when (currentConversationType) {
            V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P ->
                V2NIMConversationIdUtil.p2pConversationId(targetId)
            V2NIMConversationType.V2NIM_CONVERSATION_TYPE_TEAM ->
                V2NIMConversationIdUtil.teamConversationId(targetId)
            V2NIMConversationType.V2NIM_CONVERSATION_TYPE_SUPER_TEAM ->
                V2NIMConversationIdUtil.superTeamConversationId(targetId)
            else -> null
        }
    }

    /**
     * 发起请求
     */
    override fun onRequest() {
        val targetId = binding.edtTargetId.text.toString().trim()

        if (targetId.isEmpty()) {
            showToast("请输入目标ID")
            return
        }

        val conversationId = generateConversationId(targetId)
        if (conversationId == null) {
            showToast("无法生成会话ID，请检查登录状态")
            return
        }

        // 验证会话ID是否有效
        if (!V2NIMConversationIdUtil.isConversationIdValid(conversationId)) {
            showToast("生成的会话ID无效，请检查参数")
            return
        }

        activityViewModel.showLoadingDialog()
        NIMClient.getService(V2NIMLocalConversationService::class.java).createConversation(
            conversationId,
            { result -> updateResult(result, null) },
            { error -> updateResult(null, error) }
        )
    }

    /**
     * 更新结果
     */
    private fun updateResult(result: V2NIMLocalConversation?, error: V2NIMError?) {
        activityViewModel.dismissLoadingDialog()
        if (error != null) {
            activityViewModel.refresh("创建本地会话失败: $error")
            return
        }

        result?.let {
            val resultText = buildResultText(it)
            activityViewModel.refresh(resultText)
        }
    }

    /**
     * 构建结果文本
     */
    private fun buildResultText(conversation: V2NIMLocalConversation): String {
        val sb = StringBuilder()

        sb.append("创建本地会话成功:\n\n")
        sb.append("会话详情:\n")
        sb.append("conversationId: ${conversation.conversationId}\n")
        sb.append("type: ${conversation.type}\n")
        sb.append("name: ${conversation.name}\n")
        sb.append("avatar: ${conversation.avatar}\n")
        sb.append("createTime: ${conversation.createTime}\n")
        sb.append("updateTime: ${conversation.updateTime}\n")
        sb.append("localExtension: ${conversation.localExtension}\n")
        sb.append("lastMessage: ${getLastMessageInfo(conversation.lastMessage)}\n")
        sb.append("unreadCount: ${conversation.unreadCount}\n")
        sb.append("mute: ${conversation.isMute}\n")
        sb.append("stickTop: ${conversation.isStickTop}\n")

        // 显示会话ID分析信息
        sb.append("\n会话ID分析:\n")
        sb.append("解析类型: ${V2NIMConversationIdUtil.conversationType(conversation.conversationId)}\n")
        sb.append("目标ID: ${V2NIMConversationIdUtil.conversationTargetId(conversation.conversationId)}\n")

        return sb.toString()
    }

    /**
     * 获取最后一条消息信息
     */
    private fun getLastMessageInfo(lastMessage: V2NIMLastMessage?): String {
        return if (lastMessage == null) {
            "null"
        } else {
            "{messageType: ${lastMessage.messageType}, text: ${lastMessage.text}, senderName: ${lastMessage.senderName}, messageRefer: ${lastMessage.messageRefer?.messageClientId}}"
        }
    }
}