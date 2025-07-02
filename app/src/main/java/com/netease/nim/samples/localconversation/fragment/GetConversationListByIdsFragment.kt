package com.netease.nim.samples.localconversation.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.databinding.FragmentGetConversationListByIdsBinding
import com.netease.nim.samples.localconversation.common.V2LocalConversationMultiSelectViewModel
import com.netease.nim.samples.localconversation.common.V2NIMLocalConversationMultiSelectFragment
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.V2NIMError
import com.netease.nimlib.sdk.v2.conversation.V2NIMLocalConversationService
import com.netease.nimlib.sdk.v2.conversation.model.V2NIMLastMessage
import com.netease.nimlib.sdk.v2.conversation.model.V2NIMLocalConversation

class GetConversationListByIdsFragment : BaseMethodExecuteFragment<FragmentGetConversationListByIdsBinding>() {

    /**
     * 多选会话的 ViewModel
     */
    private lateinit var selectConversationMultiViewModel: V2LocalConversationMultiSelectViewModel

    /**
     * 创建ViewBinding，由子类实现
     */
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentGetConversationListByIdsBinding {
        return FragmentGetConversationListByIdsBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initClickListeners()
        initTextWatcher()
        updateValidationStatus()
        
        // 初始化多选会话ViewModel
        selectConversationMultiViewModel = getActivityViewModel(V2LocalConversationMultiSelectViewModel::class.java)
        selectConversationMultiViewModel.selectListLiveData.observe(viewLifecycleOwner) { conversationList ->
            activityViewModel.popFragment()
            handleSelectedConversations(conversationList)
        }
    }

    private fun initClickListeners() {
        // 选择会话按钮
        binding.btnSelectConversations.setOnClickListener {
            activityViewModel.addFragment(V2NIMLocalConversationMultiSelectFragment.NAME)
        }

        // 清空按钮
        binding.btnClearIds.setOnClickListener {
            binding.edtConversationIds.setText("")
            showToast("已清空会话ID列表")
        }

    }

    private fun initTextWatcher() {
        binding.edtConversationIds.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                updateUIStatus()
            }
        })
    }

    /**
     * 处理选择的会话列表
     */
    private fun handleSelectedConversations(conversationList: List<V2NIMLocalConversation>) {
        if (conversationList.isEmpty()) {
            showToast("未选择任何会话")
            return
        }

        val conversationIds = conversationList.map { it.conversationId }
        val currentText = binding.edtConversationIds.text.toString().trim()
        
        val newIds = if (currentText.isEmpty()) {
            conversationIds.joinToString(",")
        } else {
            "$currentText,${conversationIds.joinToString(",")}"
        }
        
        binding.edtConversationIds.setText(newIds)
        showToast("已添加 ${conversationIds.size} 个会话ID")
    }

    /**
     * 更新UI状态
     */
    private fun updateUIStatus() {
        val conversationIds = getConversationIdList()
        
        // 更新ID数量显示
        binding.tvIdsCount.text = "当前输入ID数量: ${conversationIds.size}个"
        
        // 更新ID预览
        updateIdsPreview(conversationIds)
        
        // 更新验证状态
        updateValidationStatus()
    }

    /**
     * 更新ID预览
     */
    private fun updateIdsPreview(conversationIds: List<String>) {
        if (conversationIds.isEmpty()) {
            binding.tvIdsPreview.text = "暂无ID"
            return
        }

        val previewText = if (conversationIds.size <= 5) {
            conversationIds.joinToString("\n")
        } else {
            conversationIds.take(5).joinToString("\n") + "\n... 还有${conversationIds.size - 5}个ID"
        }
        
        binding.tvIdsPreview.text = previewText
    }

    /**
     * 更新验证状态
     */
    private fun updateValidationStatus() {
        val conversationIds = getConversationIdList()
        
        if (conversationIds.isEmpty()) {
            binding.tvValidationStatus.text = "✗ 会话ID列表不能为空"
            binding.tvValidationStatus.setTextColor(resources.getColor(android.R.color.holo_red_dark))
        } else {
            val duplicates = conversationIds.groupingBy { it }.eachCount().filter { it.value > 1 }
            if (duplicates.isNotEmpty()) {
                binding.tvValidationStatus.text = "⚠ 检测到重复ID: ${duplicates.keys.joinToString(", ")}"
                binding.tvValidationStatus.setTextColor(resources.getColor(android.R.color.holo_orange_dark))
            } else {
                binding.tvValidationStatus.text = "✓ 参数验证通过，共${conversationIds.size}个有效ID"
                binding.tvValidationStatus.setTextColor(resources.getColor(android.R.color.holo_green_dark))
            }
        }
    }

    /**
     * 发起请求
     */
    override fun onRequest() {
        val conversationIds = getConversationIdList()
        
        if (conversationIds.isEmpty()) {
            showToast("请输入会话ID列表")
            return
        }

        activityViewModel.showLoadingDialog()
        NIMClient.getService(V2NIMLocalConversationService::class.java).getConversationListByIds(
            conversationIds,
            { conversationList -> updateResult(conversationList, null) },
            { error -> updateResult(null, error) }
        )
    }

    /**
     * 更新结果
     */
    private fun updateResult(conversationList: List<V2NIMLocalConversation>?, error: V2NIMError?) {
        activityViewModel.dismissLoadingDialog()
        
        if (error != null) {
            activityViewModel.refresh("查询会话列表失败: $error")
            return
        }

        conversationList?.let {
            val resultText = buildResultText(it)
            activityViewModel.refresh(resultText)
        }
    }

    /**
     * 构建结果文本
     */
    private fun buildResultText(conversationList: List<V2NIMLocalConversation>): String {
        val inputIds = getConversationIdList()
        val sb = StringBuilder()
        
        sb.append("查询会话列表成功:\n")
        sb.append("输入ID数量: ${inputIds.size}\n")
        sb.append("查询到会话数量: ${conversationList.size}\n")
        
        if (conversationList.size < inputIds.size) {
            val foundIds = conversationList.map { it.conversationId }.toSet()
            val notFoundIds = inputIds.filter { it !in foundIds }
            sb.append("未找到的ID: ${notFoundIds.joinToString(", ")}\n")
        }
        
        sb.append("\n会话详情:\n")

        conversationList.forEachIndexed { index, conversation ->
            sb.append("${index + 1}. 会话信息:\n")
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
            
            if (index < conversationList.size - 1) {
                sb.append("\n")
            }
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
     * 获取会话ID列表
     */
    private fun getConversationIdList(): List<String> {
        val idsText = binding.edtConversationIds.text.toString().trim()
        if (idsText.isEmpty()) {
            return emptyList()
        }

        // 支持逗号和换行分隔
        return idsText.split("[,\n]".toRegex())
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct() // 去重
    }
}