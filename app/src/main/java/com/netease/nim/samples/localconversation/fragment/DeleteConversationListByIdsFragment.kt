package com.netease.nim.samples.localconversation.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.databinding.FragmentDeleteConversationListByIdsBinding
import com.netease.nim.samples.localconversation.common.V2LocalConversationMultiSelectViewModel
import com.netease.nim.samples.localconversation.common.V2NIMLocalConversationMultiSelectFragment
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.V2NIMError
import com.netease.nimlib.sdk.v2.conversation.V2NIMLocalConversationService
import com.netease.nimlib.sdk.v2.conversation.model.V2NIMLocalConversation
import com.netease.nimlib.sdk.v2.conversation.result.V2NIMLocalConversationOperationResult
import com.netease.nimlib.sdk.v2.utils.V2NIMConversationIdUtil

class DeleteConversationListByIdsFragment : BaseMethodExecuteFragment<FragmentDeleteConversationListByIdsBinding>() {

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
    ): FragmentDeleteConversationListByIdsBinding {
        return FragmentDeleteConversationListByIdsBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initClickListeners()
        initTextWatcher()
        updateUIStatus()

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

        // 验证会话ID格式
        binding.btnValidateIds.setOnClickListener {
            validateConversationIds()
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
            conversationIds.joinToString("\n")
        } else {
            "$currentText\n${conversationIds.joinToString("\n")}"
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
        binding.tvCount.text = "会话数量: ${conversationIds.size}个"

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
            val invalidIds = conversationIds.filter { !V2NIMConversationIdUtil.isConversationIdValid(it) }

            when {
                invalidIds.isNotEmpty() -> {
                    binding.tvValidationStatus.text = "❌ 检测到无效ID: ${invalidIds.take(3).joinToString(", ")}${if (invalidIds.size > 3) "等" else ""}"
                    binding.tvValidationStatus.setTextColor(resources.getColor(android.R.color.holo_red_dark))
                }
                duplicates.isNotEmpty() -> {
                    binding.tvValidationStatus.text = "⚠ 检测到重复ID: ${duplicates.keys.take(3).joinToString(", ")}${if (duplicates.size > 3) "等" else ""}"
                    binding.tvValidationStatus.setTextColor(resources.getColor(android.R.color.holo_orange_dark))
                }
                else -> {
                    binding.tvValidationStatus.text = "✓ 参数验证通过，共${conversationIds.size}个有效ID"
                    binding.tvValidationStatus.setTextColor(resources.getColor(android.R.color.holo_green_dark))
                }
            }
        }
    }

    /**
     * 验证会话ID格式
     */
    private fun validateConversationIds() {
        val conversationIds = getConversationIdList()
        
        if (conversationIds.isEmpty()) {
            showToast("请先输入会话ID")
            return
        }

        val validIds = mutableListOf<String>()
        val invalidIds = mutableListOf<String>()

        conversationIds.forEach { id ->
            if (V2NIMConversationIdUtil.isConversationIdValid(id)) {
                validIds.add(id)
            } else {
                invalidIds.add(id)
            }
        }

        val resultText = buildValidationResultText(validIds, invalidIds)
        activityViewModel.refresh(resultText)
    }

    /**
     * 构建验证结果文本
     */
    private fun buildValidationResultText(validIds: List<String>, invalidIds: List<String>): String {
        val sb = StringBuilder()
        
        sb.append("会话ID格式验证结果:\n\n")
        sb.append("总计: ${validIds.size + invalidIds.size} 个会话ID\n")
        sb.append("有效: ${validIds.size} 个\n")
        sb.append("无效: ${invalidIds.size} 个\n\n")
        
        if (validIds.isNotEmpty()) {
            sb.append("✅ 有效的会话ID:\n")
            validIds.forEach { id ->
                val type = V2NIMConversationIdUtil.conversationType(id)
                val targetId = V2NIMConversationIdUtil.conversationTargetId(id)
                sb.append("• $id\n  类型: $type, 目标: $targetId\n")
            }
            sb.append("\n")
        }
        
        if (invalidIds.isNotEmpty()) {
            sb.append("❌ 无效的会话ID:\n")
            invalidIds.forEach { id ->
                sb.append("• $id\n")
            }
        }
        
        return sb.toString()
    }

    /**
     * 发起请求
     */
    override fun onRequest() {
        val conversationIds = getConversationIdList()
        val clearMessage = binding.cbClearMessage.isChecked

        if (conversationIds.isEmpty()) {
            showToast("请输入要删除的会话ID列表")
            return
        }

        // 验证所有会话ID格式
        val invalidIds = conversationIds.filter { !V2NIMConversationIdUtil.isConversationIdValid(it) }
        if (invalidIds.isNotEmpty()) {
            showToast("存在无效的会话ID，请先验证格式")
            return
        }

        // 显示确认对话框
        showDeleteConfirmation(conversationIds, clearMessage)
    }

    /**
     * 显示删除确认对话框
     */
    private fun showDeleteConfirmation(conversationIds: List<String>, clearMessage: Boolean) {
        val messageText = if (clearMessage) {
            "确定要删除 ${conversationIds.size} 个会话并清除所有消息吗？\n\n此操作不可撤销！"
        } else {
            "确定要删除 ${conversationIds.size} 个会话吗？\n\n消息将被保留"
        }

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("确认批量删除")
            .setMessage(messageText)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton("确定删除") { _, _ ->
                performBatchDelete(conversationIds, clearMessage)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    /**
     * 执行批量删除操作
     */
    private fun performBatchDelete(conversationIds: List<String>, clearMessage: Boolean) {
        activityViewModel.showLoadingDialog()
        
        NIMClient.getService(V2NIMLocalConversationService::class.java).deleteConversationListByIds(
            conversationIds,
            clearMessage,
            { results -> updateResult(results, clearMessage, conversationIds.size, null) },
            { error -> updateResult(null, clearMessage, conversationIds.size, error) }
        )
    }

    /**
     * 更新结果
     */
    private fun updateResult(
        results: List<V2NIMLocalConversationOperationResult>?, 
        clearMessage: Boolean, 
        totalCount: Int, 
        error: V2NIMError?
    ) {
        activityViewModel.dismissLoadingDialog()
        
        if (results != null) {
            val resultText = buildSuccessResultText(results, clearMessage, totalCount)
            activityViewModel.refresh(resultText)
            
            // 删除成功后清空输入框
            binding.edtConversationIds.setText("")
            binding.cbClearMessage.isChecked = false
        } else {
            val resultText = buildFailureResultText(clearMessage, totalCount, error)
            activityViewModel.refresh(resultText)
        }
    }

    /**
     * 构建成功结果文本
     */
    private fun buildSuccessResultText(
        results: List<V2NIMLocalConversationOperationResult>, 
        clearMessage: Boolean, 
        totalCount: Int
    ): String {
        val sb = StringBuilder()
        
        val successCount = results.count { it.error == null }
        val failureCount = results.count { it.error != null }
        
        sb.append("批量删除本地会话完成! 📊\n\n")
        sb.append("操作统计:\n")
        sb.append("• 总数: $totalCount\n")
        sb.append("• 成功: $successCount\n")
        sb.append("• 失败: $failureCount\n")
        sb.append("• 操作类型: ")
        if (clearMessage) {
            sb.append("删除会话并清除消息\n\n")
        } else {
            sb.append("仅删除会话记录\n\n")
        }
        
        // 成功的操作
        if (successCount > 0) {
            sb.append("✅ 删除成功的会话:\n")
            results.filter { it.error == null }.forEach { result ->
                val type = V2NIMConversationIdUtil.conversationType(result.conversationId)
                val targetId = V2NIMConversationIdUtil.conversationTargetId(result.conversationId)
                sb.append("• ${result.conversationId}\n")
                sb.append("  类型: $type, 目标: $targetId\n")
            }
            sb.append("\n")
        }
        
        // 失败的操作
        if (failureCount > 0) {
            sb.append("❌ 删除失败的会话:\n")
            results.filter { it.error != null }.forEach { result ->
                sb.append("• ${result.conversationId}\n")
                sb.append("  错误: ${result.error?.desc ?: "未知错误"} (${result.error?.code})\n")
            }
        }
        
        return sb.toString()
    }

    /**
     * 构建失败结果文本
     */
    private fun buildFailureResultText(clearMessage: Boolean, totalCount: Int, error: V2NIMError?): String {
        val sb = StringBuilder()
        
        sb.append("批量删除本地会话失败! ❌\n\n")
        sb.append("操作详情:\n")
        sb.append("• 总会话数: $totalCount\n")
        sb.append("• 操作类型: ")
        if (clearMessage) {
            sb.append("删除会话并清除消息\n")
        } else {
            sb.append("仅删除会话记录\n")
        }
        
        sb.append("• 错误信息: ${error?.desc ?: "未知错误"}\n")
        sb.append("• 错误代码: ${error?.code ?: "N/A"}\n\n")
        
        sb.append("可能原因:\n")
        sb.append("• 网络连接问题\n")
        sb.append("• 用户未登录\n")
        sb.append("• 服务异常\n")
        sb.append("• 参数格式错误\n")
        
        return sb.toString()
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