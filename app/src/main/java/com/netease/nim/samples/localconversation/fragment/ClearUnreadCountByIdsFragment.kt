package com.netease.nim.samples.localconversation.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.databinding.FragmentClearUnreadCountByIdsBinding
import com.netease.nim.samples.localconversation.common.V2LocalConversationMultiSelectViewModel
import com.netease.nim.samples.localconversation.common.V2NIMLocalConversationMultiSelectFragment
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.V2NIMError
import com.netease.nimlib.sdk.v2.conversation.V2NIMLocalConversationService
import com.netease.nimlib.sdk.v2.conversation.model.V2NIMLocalConversation
import com.netease.nimlib.sdk.v2.conversation.result.V2NIMLocalConversationOperationResult

class ClearUnreadCountByIdsFragment : BaseMethodExecuteFragment<FragmentClearUnreadCountByIdsBinding>() {

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
    ): FragmentClearUnreadCountByIdsBinding {
        return FragmentClearUnreadCountByIdsBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initClickListeners()
        initTextWatcher()

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
     * 发起请求
     */
    override fun onRequest() {
        val conversationIds = getConversationIdList()

        if (conversationIds.isEmpty()) {
            showToast("请输入会话ID列表")
            return
        }

        activityViewModel.showLoadingDialog()
        NIMClient.getService(V2NIMLocalConversationService::class.java).clearUnreadCountByIds(
            conversationIds,
            { operationResults -> updateResult(operationResults, null) },
            { error -> updateResult(null, error) }
        )
    }

    /**
     * 更新结果
     */
    private fun updateResult(operationResults: List<V2NIMLocalConversationOperationResult>?, error: V2NIMError?) {
        activityViewModel.dismissLoadingDialog()

        if (error != null) {
            activityViewModel.refresh("清空会话未读数失败: $error")
            return
        }

        operationResults?.let {
            
            val resultText = buildResultText(it)
            activityViewModel.refresh(resultText)
        }
    }

    /**
     * 构建结果文本
     */
    private fun buildResultText(operationResults: List<V2NIMLocalConversationOperationResult>): String {
        val inputIds = getConversationIdList()
        val sb = StringBuilder()

        sb.append("清空会话未读数操作完成:\n")
        sb.append("输入ID数量: ${inputIds.size}\n")
        sb.append("操作失败数量: ${operationResults.size}\n")
        sb.append("操作时间: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date())}\n")
        sb.append("\n详细结果:\n")
        operationResults.forEachIndexed { index, result ->
            sb.append("${index + 1}. 会话ID: ${result.conversationId}\n")
            sb.append("   error: ${result.error}\n")
            sb.append("\n")
        }

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