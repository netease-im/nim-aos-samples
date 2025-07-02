package com.netease.nim.samples.localconversation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.databinding.FragmentDeleteConversationBinding
import com.netease.nim.samples.localconversation.common.V2LocalConversationSelectViewModel
import com.netease.nim.samples.localconversation.common.V2NIMLocalConversationSelectFragment
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.V2NIMError
import com.netease.nimlib.sdk.v2.conversation.V2NIMLocalConversationService
import com.netease.nimlib.sdk.v2.utils.V2NIMConversationIdUtil

class DeleteConversationFragment : BaseMethodExecuteFragment<FragmentDeleteConversationBinding>() {

    /**
     * 选择会话的 ViewModel
     */
    private lateinit var selectConversationViewModel: V2LocalConversationSelectViewModel

    /**
     * 创建ViewBinding，由子类实现
     */
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDeleteConversationBinding {
        return FragmentDeleteConversationBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initClickListeners()
        initConversationSelection()
    }

    private fun initClickListeners() {
        // 选择会话按钮
        binding.btnSelectConversation.setOnClickListener {
            activityViewModel.addFragment(V2NIMLocalConversationSelectFragment.NAME)
        }
    }

    /**
     * 初始化会话选择功能
     */
    private fun initConversationSelection() {
        selectConversationViewModel = getActivityViewModel(V2LocalConversationSelectViewModel::class.java)
        selectConversationViewModel.selectItemLiveData.observe(viewLifecycleOwner) { conversationItem ->
            // 从选择的会话中填入会话ID
            activityViewModel.popFragment()
            binding.edtConversationId.setText(conversationItem.conversationId)
            showToast("已选择会话: ${conversationItem.name ?: conversationItem.conversationId}")
        }
    }

    /**
     * 发起请求
     */
    override fun onRequest() {
        val conversationId = getConversationId()
        val clearMessage = binding.cbClearMessage.isChecked

        if (conversationId.isEmpty()) {
            showToast("请输入或选择会话ID")
            return
        }

        // 验证会话ID格式
        if (!V2NIMConversationIdUtil.isConversationIdValid(conversationId)) {
            showToast("会话ID格式不正确")
            return
        }

        // 显示确认对话框
        showDeleteConfirmation(conversationId, clearMessage)
    }

    /**
     * 显示删除确认对话框
     */
    private fun showDeleteConfirmation(conversationId: String, clearMessage: Boolean) {
        val messageText = if (clearMessage) {
            "确定要删除会话并清除所有消息吗？\n\n会话ID: $conversationId\n\n此操作不可撤销！"
        } else {
            "确定要删除会话吗？\n\n会话ID: $conversationId\n\n消息将被保留"
        }

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("确认删除")
            .setMessage(messageText)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton("确定删除") { _, _ ->
                performDelete(conversationId, clearMessage)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    /**
     * 执行删除操作
     */
    private fun performDelete(conversationId: String, clearMessage: Boolean) {
        activityViewModel.showLoadingDialog()
        
        NIMClient.getService(V2NIMLocalConversationService::class.java).deleteConversation(
            conversationId,
            clearMessage,
            { updateResult(true, clearMessage, conversationId, null) },
            { error -> updateResult(false, clearMessage, conversationId, error) }
        )
    }

    /**
     * 更新结果
     */
    private fun updateResult(success: Boolean, clearMessage: Boolean, conversationId: String, error: V2NIMError?) {
        activityViewModel.dismissLoadingDialog()
        
        if (success) {
            val resultText = buildSuccessResultText(conversationId, clearMessage)
            activityViewModel.refresh(resultText)
            
            // 删除成功后清空输入框
            binding.edtConversationId.setText("")
            binding.cbClearMessage.isChecked = false
        } else {
            val resultText = buildFailureResultText(conversationId, clearMessage, error)
            activityViewModel.refresh(resultText)
        }
    }

    /**
     * 构建成功结果文本
     */
    private fun buildSuccessResultText(conversationId: String, clearMessage: Boolean): String {
        val sb = StringBuilder()
        
        sb.append("删除本地会话成功! ✅\n\n")
        sb.append("操作详情:\n")
        sb.append("• 会话ID: $conversationId\n")
        sb.append("• 操作类型: ")
        if (clearMessage) {
            sb.append("删除会话并清除消息\n")
            sb.append("• 结果: 会话和所有相关消息已被删除\n")
        } else {
            sb.append("仅删除会话记录\n")
            sb.append("• 结果: 会话已删除，消息数据保留\n")
        }
        
        // 会话ID分析
        val conversationType = V2NIMConversationIdUtil.conversationType(conversationId)
        val targetId = V2NIMConversationIdUtil.conversationTargetId(conversationId)
        
        sb.append("\n会话信息:\n")
        sb.append("• 会话类型: $conversationType\n")
        sb.append("• 目标ID: $targetId\n")
        sb.append("• 操作时间: ${System.currentTimeMillis()}\n")
        
        return sb.toString()
    }

    /**
     * 构建失败结果文本
     */
    private fun buildFailureResultText(conversationId: String, clearMessage: Boolean, error: V2NIMError?): String {
        val sb = StringBuilder()
        
        sb.append("删除本地会话失败! ❌\n\n")
        sb.append("操作详情:\n")
        sb.append("• 会话ID: $conversationId\n")
        sb.append("• 操作类型: ")
        if (clearMessage) {
            sb.append("删除会话并清除消息\n")
        } else {
            sb.append("仅删除会话记录\n")
        }
        
        sb.append("• 错误信息: ${error?.desc ?: "未知错误"}\n")
        sb.append("• 错误代码: ${error?.code ?: "N/A"}\n")
        
        // 可能的错误原因分析
        sb.append("\n可能原因:\n")
        sb.append("• 会话ID不存在\n")
        sb.append("• 会话ID格式不正确\n")
        sb.append("• 网络连接问题\n")
        sb.append("• 用户未登录\n")
        
        return sb.toString()
    }

    /**
     * 获取会话ID
     */
    private fun getConversationId(): String {
        return binding.edtConversationId.text.toString().trim()
    }
}