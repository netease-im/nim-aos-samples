package com.netease.nim.samples.localconversation.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.databinding.FragmentGetConversationReadTimeBinding
import com.netease.nim.samples.localconversation.common.V2LocalConversationSelectViewModel
import com.netease.nim.samples.localconversation.common.V2NIMLocalConversationSelectFragment
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.V2NIMError
import com.netease.nimlib.sdk.v2.conversation.V2NIMLocalConversationService
import com.netease.nimlib.sdk.v2.conversation.model.V2NIMLocalConversation
import com.netease.nimlib.sdk.v2.utils.V2NIMConversationIdUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GetConversationReadTimeFragment : BaseMethodExecuteFragment<FragmentGetConversationReadTimeBinding>() {

    /**
     * 单选会话的 ViewModel
     */
    private lateinit var selectConversationSingleViewModel: V2LocalConversationSelectViewModel

    /**
     * 创建ViewBinding，由子类实现
     */
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentGetConversationReadTimeBinding {
        return FragmentGetConversationReadTimeBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initClickListeners()
        initTextWatcher()
        updateValidationStatus()

        // 初始化单选会话ViewModel
        selectConversationSingleViewModel = getActivityViewModel(V2LocalConversationSelectViewModel::class.java)
        selectConversationSingleViewModel.selectItemLiveData.observe(viewLifecycleOwner) { conversation ->
            activityViewModel.popFragment()
            handleSelectedConversation(conversation)
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
    private fun handleSelectedConversation(conversation: V2NIMLocalConversation?) {
        if (conversation == null) {
            showToast("未选择任何会话")
            return
        }

        binding.edtConversationId.setText(conversation.conversationId)
        showToast("已选择会话: ${conversation.name ?: conversation.conversationId}")
    }

    /**
     * 更新UI状态
     */
    private fun updateUIStatus() {
        val conversationId = getConversationId()

        // 更新ID长度显示
        binding.tvIdLength.text = "当前输入长度: ${conversationId.length}个字符"

        // 更新ID解析
        updateIdAnalysis(conversationId)

        // 更新验证状态
        updateValidationStatus()
    }

    /**
     * 更新ID解析
     */
    private fun updateIdAnalysis(conversationId: String) {
        if (conversationId.isEmpty()) {
            binding.tvIdAnalysis.text = "请输入会话ID"
            return
        }

        val analysis = analyzeConversationId(conversationId)
        binding.tvIdAnalysis.text = analysis
    }

    /**
     * 分析会话ID
     */
    private fun analyzeConversationId(conversationId: String): String {
        try {
            val valid = V2NIMConversationIdUtil.isConversationIdValid(conversationId)
            if (valid) {
                val type = V2NIMConversationIdUtil.conversationType(conversationId)
                val targetId = V2NIMConversationIdUtil.conversationTargetId(conversationId)
                return "格式: 标准会话ID格式\n类型: $type\n目标ID: $targetId"
            } else {
                return "格式: 非标准会话ID格式\n原始ID: $conversationId\n⚠ 请使用标准格式"
            }
        } catch (e: Exception) {
            return "解析失败: ${e.message}\n原始ID: $conversationId"
        }
    }

    /**
     * 更新验证状态
     */
    private fun updateValidationStatus() {
        val conversationId = getConversationId()

        when {
            conversationId.isEmpty() -> {
                binding.tvValidationStatus.text = "✗ 会话ID不能为空"
                binding.tvValidationStatus.setTextColor(resources.getColor(android.R.color.holo_red_dark))
            }
            !isSupportedConversationType(conversationId) -> {
                binding.tvValidationStatus.text = "⚠ 该会话类型可能不支持查询已读时间"
                binding.tvValidationStatus.setTextColor(resources.getColor(android.R.color.holo_orange_dark))
            }
            else -> {
                binding.tvValidationStatus.text = "✓ 参数验证通过，支持查询已读时间"
                binding.tvValidationStatus.setTextColor(resources.getColor(android.R.color.holo_green_dark))
            }
        }
    }

    /**
     * 检查是否为支持的会话类型
     */
    private fun isSupportedConversationType(conversationId: String): Boolean {
        val supportedTypes = listOf("p2p", "team", "superteam")
        return try {
            val type = conversationId.split("|")[0].lowercase()
            supportedTypes.contains(type)
        } catch (e: Exception) {
            false
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

        activityViewModel.showLoadingDialog()
        NIMClient.getService(V2NIMLocalConversationService::class.java).getConversationReadTime(
            conversationId,
            { readTime -> updateResult(readTime, null) },
            { error -> updateResult(null, error) }
        )
    }

    /**
     * 更新结果
     */
    private fun updateResult(readTime: Long?, error: V2NIMError?) {
        activityViewModel.dismissLoadingDialog()

        if (error != null) {
            activityViewModel.refresh("获取会话已读时间失败: $error")
            return
        }

        readTime?.let {
            val resultText = buildResultText(it)
            activityViewModel.refresh(resultText)
        }
    }

    /**
     * 构建结果文本
     */
    private fun buildResultText(readTime: Long): String {
        val conversationId = getConversationId()
        val sb = StringBuilder()

        sb.append("获取会话已读时间成功:\n")
        sb.append("会话ID: $conversationId\n")
        sb.append("已读时间戳: $readTime\n")

        // 格式化时间戳
        if (readTime > 0) {
            val date = Date(readTime)
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
            sb.append("格式化时间: ${formatter.format(date)}\n")
        } else {
            sb.append("格式化时间: 未设置（时间戳为0）\n")
            sb.append("说明: 该会话从未被标记为已读\n")
        }

        return sb.toString()
    }

    /**
     * 获取会话ID
     */
    private fun getConversationId(): String {
        return binding.edtConversationId.text.toString().trim()
    }
}