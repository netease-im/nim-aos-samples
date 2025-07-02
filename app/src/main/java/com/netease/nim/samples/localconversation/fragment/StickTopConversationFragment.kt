package com.netease.nim.samples.localconversation.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.databinding.FragmentStickTopConversationBinding
import com.netease.nim.samples.localconversation.common.V2LocalConversationSelectViewModel
import com.netease.nim.samples.localconversation.common.V2NIMLocalConversationSelectFragment
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.V2NIMError
import com.netease.nimlib.sdk.v2.conversation.V2NIMLocalConversationService
import com.netease.nimlib.sdk.v2.utils.V2NIMConversationIdUtil

class StickTopConversationFragment : BaseMethodExecuteFragment<FragmentStickTopConversationBinding>() {

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
    ): FragmentStickTopConversationBinding {
        return FragmentStickTopConversationBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initClickListeners()
        initTextWatcher()
        updateValidationStatus()
        
        // 初始化选择会话ViewModel
        selectConversationViewModel = getActivityViewModel(V2LocalConversationSelectViewModel::class.java)
        selectConversationViewModel.selectItemLiveData.observe(viewLifecycleOwner) { conversationItem ->
            activityViewModel.popFragment()
            handleSelectedConversation(conversationItem.conversationId)
        }
    }

    private fun initClickListeners() {
        // 选择会话按钮
        binding.btnSelectConversation.setOnClickListener {
            activityViewModel.addFragment(V2NIMLocalConversationSelectFragment.NAME)
        }

        // 清空按钮
        binding.btnClearId.setOnClickListener {
            binding.edtConversationId.setText("")
            showToast("已清空会话ID")
        }
    }

    private fun initTextWatcher() {
        binding.edtConversationId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                updateUIStatus()
            }
        })
    }

    /**
     * 处理选择的会话
     */
    private fun handleSelectedConversation(conversationId: String) {
        binding.edtConversationId.setText(conversationId)
        showToast("已选择会话: $conversationId")
    }

    /**
     * 更新UI状态
     */
    private fun updateUIStatus() {
        val conversationId = getConversationId()
        
        // 更新ID状态显示
        updateIdStatus(conversationId)
        
        // 更新ID预览
        updateIdPreview(conversationId)
        
        // 更新验证状态
        updateValidationStatus()
    }

    /**
     * 更新ID状态显示
     */
    private fun updateIdStatus(conversationId: String) {
        if (conversationId.isEmpty()) {
            binding.tvIdStatus.text = "当前输入状态: 无输入"
        } else {
            binding.tvIdStatus.text = "当前输入状态: 已输入会话ID"
        }
    }

    /**
     * 更新ID预览
     */
    private fun updateIdPreview(conversationId: String) {
        if (conversationId.isEmpty()) {
            binding.tvIdPreview.text = "暂无ID"
            return
        }

        if (V2NIMConversationIdUtil.isConversationIdValid(conversationId)) {
            val type = V2NIMConversationIdUtil.conversationType(conversationId)
            val targetId = V2NIMConversationIdUtil.conversationTargetId(conversationId)
            binding.tvIdPreview.text = "会话ID: $conversationId\n会话类型: $type\n目标ID: $targetId"
        } else {
            binding.tvIdPreview.text = "会话ID: $conversationId\n❌ 格式无效"
        }
    }

    /**
     * 更新验证状态
     */
    private fun updateValidationStatus() {
        val conversationId = getConversationId()
        
        if (conversationId.isEmpty()) {
            binding.tvValidationStatus.text = "✗ 会话ID不能为空"
            binding.tvValidationStatus.setTextColor(resources.getColor(android.R.color.holo_red_dark))
        } else if (!V2NIMConversationIdUtil.isConversationIdValid(conversationId)) {
            binding.tvValidationStatus.text = "❌ 会话ID格式无效"
            binding.tvValidationStatus.setTextColor(resources.getColor(android.R.color.holo_red_dark))
        } else {
            val stickTop = binding.rbStickTop.isChecked
            val operation = if (stickTop) "置顶" else "取消置顶"
            binding.tvValidationStatus.text = "✓ 参数验证通过，将执行${operation}操作"
            binding.tvValidationStatus.setTextColor(resources.getColor(android.R.color.holo_green_dark))
        }
    }

    /**
     * 发起请求
     */
    override fun onRequest() {
        val conversationId = getConversationId()
        
        if (conversationId.isEmpty()) {
            showToast("请输入会话ID")
            return
        }

        if (!V2NIMConversationIdUtil.isConversationIdValid(conversationId)) {
            showToast("会话ID格式无效")
            return
        }

        val stickTop = binding.rbStickTop.isChecked
        val operation = if (stickTop) "置顶" else "取消置顶"
        
        // 显示确认对话框
        showOperationConfirmation(conversationId, stickTop, operation)
    }

    /**
     * 显示操作确认对话框
     */
    private fun showOperationConfirmation(conversationId: String, stickTop: Boolean, operation: String) {
        val type = V2NIMConversationIdUtil.conversationType(conversationId)
        val targetId = V2NIMConversationIdUtil.conversationTargetId(conversationId)
        
        val messageText = "确定要${operation}此会话吗？\n\n会话信息：\n类型：$type\n目标：$targetId\nID：$conversationId"

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("确认${operation}操作")
            .setMessage(messageText)
            .setIcon(if (stickTop) android.R.drawable.ic_menu_set_as else android.R.drawable.ic_menu_revert)
            .setPositiveButton("确定${operation}") { _, _ ->
                performStickTopOperation(conversationId, stickTop)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    /**
     * 执行置顶操作
     */
    private fun performStickTopOperation(conversationId: String, stickTop: Boolean) {
        activityViewModel.showLoadingDialog()
        
        NIMClient.getService(V2NIMLocalConversationService::class.java).stickTopConversation(
            conversationId,
            stickTop,
            { updateResult(conversationId, stickTop, null) },
            { error -> updateResult(conversationId, stickTop, error) }
        )
    }

    /**
     * 更新结果
     */
    private fun updateResult(conversationId: String, stickTop: Boolean, error: V2NIMError?) {
        activityViewModel.dismissLoadingDialog()
        
        if (error != null) {
            val operation = if (stickTop) "置顶" else "取消置顶"
            val resultText = buildFailureResultText(conversationId, stickTop, operation, error)
            activityViewModel.refresh(resultText)
        } else {
            val resultText = buildSuccessResultText(conversationId, stickTop)
            activityViewModel.refresh(resultText)
            
            // 操作成功后清空输入框
            binding.edtConversationId.setText("")
        }
    }

    /**
     * 构建成功结果文本
     */
    private fun buildSuccessResultText(conversationId: String, stickTop: Boolean): String {
        val sb = StringBuilder()
        val operation = if (stickTop) "置顶" else "取消置顶"
        val type = V2NIMConversationIdUtil.conversationType(conversationId)
        val targetId = V2NIMConversationIdUtil.conversationTargetId(conversationId)
        
        sb.append("${operation}会话成功! 🎉\n\n")
        sb.append("操作详情:\n")
        sb.append("• 会话ID: $conversationId\n")
        sb.append("• 会话类型: $type\n")
        sb.append("• 目标ID: $targetId\n")
        sb.append("• 操作类型: $operation\n")
        sb.append("• 置顶状态: ${if (stickTop) "已置顶" else "已取消置顶"}\n\n")
        
        if (stickTop) {
            sb.append("✅ 置顶效果:\n")
            sb.append("• 该会话将显示在会话列表顶部\n")
            sb.append("• 会话排序优先级提升\n")
            sb.append("• 会话列表会自动刷新显示\n")
        } else {
            sb.append("✅ 取消置顶效果:\n")
            sb.append("• 该会话恢复正常排序位置\n")
            sb.append("• 按照最后消息时间排序\n")
            sb.append("• 会话列表会自动刷新显示\n")
        }
        
        sb.append("\n💡 提示:\n")
        sb.append("• 可以在会话列表中查看置顶效果\n")
        sb.append("• 置顶状态会持久化保存\n")
        
        return sb.toString()
    }

    /**
     * 构建失败结果文本
     */
    private fun buildFailureResultText(conversationId: String, stickTop: Boolean, operation: String, error: V2NIMError?): String {
        val sb = StringBuilder()
        val type = V2NIMConversationIdUtil.conversationType(conversationId)
        val targetId = V2NIMConversationIdUtil.conversationTargetId(conversationId)
        
        sb.append("${operation}会话失败! ❌\n\n")
        sb.append("操作详情:\n")
        sb.append("• 会话ID: $conversationId\n")
        sb.append("• 会话类型: $type\n")
        sb.append("• 目标ID: $targetId\n")
        sb.append("• 操作类型: $operation\n")
        sb.append("• 错误信息: ${error?.desc ?: "未知错误"}\n")
        sb.append("• 错误代码: ${error?.code ?: "N/A"}\n\n")
        
        sb.append("可能原因:\n")
        sb.append("• 会话不存在或已被删除\n")
        sb.append("• 网络连接问题\n")
        sb.append("• 用户未登录\n")
        sb.append("• 会话ID格式错误\n")
        sb.append("• 服务异常\n\n")
        
        sb.append("解决建议:\n")
        sb.append("• 检查会话ID是否正确\n")
        sb.append("• 确认网络连接正常\n")
        sb.append("• 验证用户登录状态\n")
        sb.append("• 重试操作或联系技术支持\n")
        
        return sb.toString()
    }

    /**
     * 获取会话ID
     */
    private fun getConversationId(): String {
        return binding.edtConversationId.text.toString().trim()
    }
}