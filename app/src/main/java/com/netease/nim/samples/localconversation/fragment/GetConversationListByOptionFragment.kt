package com.netease.nim.samples.localconversation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.databinding.FragmentGetConversationListByOptionBinding
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.V2NIMError
import com.netease.nimlib.sdk.v2.conversation.V2NIMLocalConversationService
import com.netease.nimlib.sdk.v2.conversation.enums.V2NIMConversationType
import com.netease.nimlib.sdk.v2.conversation.model.V2NIMLastMessage
import com.netease.nimlib.sdk.v2.conversation.option.V2NIMLocalConversationOption
import com.netease.nimlib.sdk.v2.conversation.result.V2NIMLocalConversationResult

class GetConversationListByOptionFragment : BaseMethodExecuteFragment<FragmentGetConversationListByOptionBinding>() {
    
    /**
     * 上一次返回的偏移量，用于分页
     */
    private var lastOffset: Long = 0L

    /**
     * 创建ViewBinding，由子类实现
     */
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentGetConversationListByOptionBinding {
        return FragmentGetConversationListByOptionBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initClickListeners()
        updatePageInfo()
    }

    private fun initClickListeners() {
        // 首页按钮
        binding.btnFirstPage.setOnClickListener {
            resetToFirstPage()
        }

        // 下一页按钮
        binding.btnNextPage.setOnClickListener {
            useLastOffset()
        }

        // 使用上次偏移按钮
        binding.btnUseLastOffset.setOnClickListener {
            useLastOffset()
        }

        // 监听偏移量和数量输入变化
        binding.edtOffset.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                updatePageInfo()
            }
        }

        binding.edtLimit.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                updatePageInfo()
            }
        }
    }

    /**
     * 发起请求
     */
    override fun onRequest() {
        val offset = getOffset()
        val limit = getLimit()
        val option = buildLocalConversationOption()

        if (limit <= 0) {
            showToast("拉取数量必须大于0")
            return
        }

        if (limit > 100) {
            showToast("不建议拉取数量超过100")
        }

        activityViewModel.showLoadingDialog()
        NIMClient.getService(V2NIMLocalConversationService::class.java).getConversationListByOption(
            offset,
            limit,
            option,
            { result -> updateResult(result, null) },
            { error -> updateResult(null, error) }
        )
    }

    /**
     * 更新结果
     */
    private fun updateResult(result: V2NIMLocalConversationResult?, error: V2NIMError?) {
        activityViewModel.dismissLoadingDialog()
        if (error != null) {
            activityViewModel.refresh("查询会话列表失败: $error")
            return
        }

        result?.let {
            // 保存返回的偏移量用于下次分页
            lastOffset = it.offset
            
            val resultText = buildResultText(it)
            activityViewModel.refresh(resultText)
            updatePageInfo()
        }
    }

    /**
     * 构建查询选项
     */
    private fun buildLocalConversationOption(): V2NIMLocalConversationOption {
        val conversationTypes = mutableListOf<V2NIMConversationType>()
        
        // 获取选中的会话类型
        if (binding.cbTypeP2p.isChecked) {
            conversationTypes.add(V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P)
        }
        if (binding.cbTypeTeam.isChecked) {
            conversationTypes.add(V2NIMConversationType.V2NIM_CONVERSATION_TYPE_TEAM)
        }
        if (binding.cbTypeSuperTeam.isChecked) {
            conversationTypes.add(V2NIMConversationType.V2NIM_CONVERSATION_TYPE_SUPER_TEAM)
        }

        val onlyUnread = binding.cbOnlyUnread.isChecked

        return V2NIMLocalConversationOption(
            if (conversationTypes.isEmpty()) null else conversationTypes,
            onlyUnread
        )
    }

    /**
     * 构建结果文本
     */
    private fun buildResultText(result: V2NIMLocalConversationResult): String {
        val conversationList = result.conversationList
        val sb = StringBuilder()
        
        sb.append("查询会话列表成功:\n")
        sb.append("下一次偏移量: ${result.offset}\n")
        sb.append("是否拉取完毕: ${result.isFinished}\n")
        sb.append("会话数量: ${conversationList?.size ?: 0}\n\n")

        conversationList?.forEachIndexed { index, conversation ->
            sb.append("${index + 1}. 会话详情:\n")
            sb.append("   conversationId: ${conversation.conversationId}\n")
            sb.append("   type: ${conversation.type}\n")
            sb.append("   name: ${conversation.name}\n")
            sb.append("   avatar: ${conversation.avatar}\n")
            sb.append("   createTime: ${conversation.createTime}\n")
            sb.append("   updateTime: ${conversation.updateTime}\n")
            sb.append("   localExtension: ${conversation.localExtension}\n")
            sb.append("   lastMessage: ${getLastMessageInfo(conversation.lastMessage)}\n")
            sb.append("   unreadCount: ${conversation.unreadCount}\n")
            sb.append("   mute: ${conversation.isMute}\n")
            sb.append("   stickTop: ${conversation.isStickTop}\n")
        }

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

    /**
     * 获取偏移量
     */
    private fun getOffset(): Long {
        return try {
            binding.edtOffset.text.toString().toLongOrNull() ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * 获取拉取数量
     */
    private fun getLimit(): Int {
        return try {
            binding.edtLimit.text.toString().toIntOrNull() ?: 50
        } catch (e: Exception) {
            50
        }
    }

    /**
     * 重置到首页
     */
    private fun resetToFirstPage() {
        lastOffset = 0L
        binding.edtOffset.setText("0")
        updatePageInfo()
        showToast("已重置到首页")
    }

    /**
     * 使用上次返回的偏移量
     */
    private fun useLastOffset() {
        binding.edtOffset.setText(lastOffset.toString())
        updatePageInfo()
        showToast("已设置为上次返回的偏移量: $lastOffset")
    }

    /**
     * 更新分页信息显示
     */
    private fun updatePageInfo() {
        val currentOffset = getOffset()
        val currentLimit = getLimit()
        binding.tvPageInfo.text = "当前偏移: $currentOffset | 拉取数量: $currentLimit | 上次偏移: $lastOffset"
    }
}