package com.netease.nim.samples.localconversation.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.databinding.FragmentMarkConversationReadBinding
import com.netease.nim.samples.localconversation.common.V2LocalConversationSelectViewModel
import com.netease.nim.samples.localconversation.common.V2NIMLocalConversationSelectFragment
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.V2NIMError
import com.netease.nimlib.sdk.v2.conversation.V2NIMLocalConversationService
import com.netease.nimlib.sdk.v2.conversation.model.V2NIMLocalConversation
import com.netease.nimlib.sdk.v2.utils.V2NIMConversationIdUtil
import java.text.SimpleDateFormat
import java.util.*

class MarkConversationReadFragment : BaseMethodExecuteFragment<FragmentMarkConversationReadBinding>() {

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
    ): FragmentMarkConversationReadBinding {
        return FragmentMarkConversationReadBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initClickListeners()
        initTextWatcher()

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

        // 查看当前已读时间按钮
        binding.btnCheckCurrentReadTime.setOnClickListener {
            checkCurrentReadTime()
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
        
        // 自动更新会话状态
        updateConversationStatus(conversation)
    }

    /**
     * 更新会话状态显示
     */
    private fun updateConversationStatus(conversation: V2NIMLocalConversation) {
        NIMClient.getService(V2NIMLocalConversationService::class.java).getConversationReadTime(
            conversation.conversationId,
            { readTime ->
                run {
                    val unreadCount = conversation.unreadCount

                    if (readTime > 0) {
                        val date = Date(readTime)
                        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        binding.tvCurrentReadTime.text =
                            "当前已读时间: ${formatter.format(date)} (${readTime})"
                    } else {
                        binding.tvCurrentReadTime.text = "当前已读时间: 未设置 (时间戳: 0)"
                    }

                    binding.tvUnreadCount.text = "未读数: $unreadCount"
                }
            },
            null
        )

    }

    /**
     * 检查当前已读时间
     */
    private fun checkCurrentReadTime() {
        val conversationId = getConversationId()
        if (conversationId.isEmpty()) {
            showToast("请先输入会话ID")
            return
        }

        NIMClient.getService(V2NIMLocalConversationService::class.java).getConversationReadTime(
            conversationId,
            { readTime ->
                if (readTime > 0) {
                    val date = Date(readTime)
                    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    binding.tvCurrentReadTime.text = "当前已读时间: ${formatter.format(date)} (${readTime})"
                } else {
                    binding.tvCurrentReadTime.text = "当前已读时间: 未设置 (时间戳: 0)"
                }
                showToast("已读时间查询成功")
            },
            { error ->
                binding.tvCurrentReadTime.text = "当前已读时间: 查询失败 ($error)"
                showToast("查询已读时间失败")
            }
        )

        // 同时查询未读数
        NIMClient.getService(V2NIMLocalConversationService::class.java).getUnreadCountByIds(
            listOf(conversationId),
            { unreadCount ->
                binding.tvUnreadCount.text = "未读数: $unreadCount"
            },
            { error ->
                binding.tvUnreadCount.text = "未读数: 查询失败 ($error)"
            }
        )
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

    }

    /**
     * 更新ID解析
     */
    private fun updateIdAnalysis(conversationId: String) {
        if (conversationId.isEmpty()) {
            binding.tvIdAnalysis.text = "请输入会话ID"
            binding.tvCurrentReadTime.text = "请先输入会话ID查看当前已读时间"
            binding.tvUnreadCount.text = "未读数: 未知"
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
     * 发起请求
     */
    override fun onRequest() {
        val conversationId = getConversationId()

        if (conversationId.isEmpty()) {
            showToast("请输入会话ID")
            return
        }

        activityViewModel.showLoadingDialog()
        NIMClient.getService(V2NIMLocalConversationService::class.java).markConversationRead(
            conversationId,
            { markReadTime -> updateResult(markReadTime, null) },
            { error -> updateResult(null, error) }
        )
    }

    /**
     * 更新结果
     */
    private fun updateResult(markReadTime: Long?, error: V2NIMError?) {
        activityViewModel.dismissLoadingDialog()

        if (error != null) {
            activityViewModel.refresh("标记会话已读失败: $error")
            return
        }

        markReadTime?.let {
            
            // 更新当前状态显示
            val date = Date(it)
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            binding.tvCurrentReadTime.text = "当前已读时间: ${formatter.format(date)} (${it})"
            binding.tvUnreadCount.text = "未读数: 0 (已标记为已读)"
            
            val resultText = buildResultText(it)
            activityViewModel.refresh(resultText)
        }
    }

    /**
     * 构建结果文本
     */
    private fun buildResultText(markReadTime: Long): String {
        val conversationId = getConversationId()
        val sb = StringBuilder()

        sb.append("标记会话已读成功:\n")
        sb.append("会话ID: $conversationId\n")
        sb.append("标记已读时间戳: $markReadTime\n")

        // 格式化时间戳
        val date = Date(markReadTime)
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
        sb.append("格式化时间: ${formatter.format(date)}\n")
        sb.append("操作时间: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())}\n")

        return sb.toString()
    }

    /**
     * 获取会话ID
     */
    private fun getConversationId(): String {
        return binding.edtConversationId.text.toString().trim()
    }
}