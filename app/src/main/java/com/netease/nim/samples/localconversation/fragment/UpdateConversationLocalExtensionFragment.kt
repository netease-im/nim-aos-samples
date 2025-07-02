package com.netease.nim.samples.localconversation.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.databinding.FragmentUpdateConversationLocalExtensionBinding
import com.netease.nim.samples.localconversation.common.V2LocalConversationSelectViewModel
import com.netease.nim.samples.localconversation.common.V2NIMLocalConversationSelectFragment
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.V2NIMError
import com.netease.nimlib.sdk.v2.conversation.V2NIMLocalConversationService
import com.netease.nimlib.sdk.v2.utils.V2NIMConversationIdUtil
import org.json.JSONObject
import org.json.JSONException

class UpdateConversationLocalExtensionFragment : BaseMethodExecuteFragment<FragmentUpdateConversationLocalExtensionBinding>() {

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
    ): FragmentUpdateConversationLocalExtensionBinding {
        return FragmentUpdateConversationLocalExtensionBinding.inflate(inflater, container, false)
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
            handleSelectedConversation(conversationItem.conversationId, conversationItem.localExtension)
        }
    }

    private fun initClickListeners() {
        // 选择会话按钮
        binding.btnSelectConversation.setOnClickListener {
            activityViewModel.addFragment(V2NIMLocalConversationSelectFragment.NAME)
        }

        // 清空ID按钮
        binding.btnClearId.setOnClickListener {
            binding.edtConversationId.setText("")
            showToast("已清空会话ID")
        }

        // 格式化JSON按钮
        binding.btnFormatJson.setOnClickListener {
            formatJsonContent()
        }

        // 清空扩展按钮
        binding.btnClearExtension.setOnClickListener {
            binding.edtLocalExtension.setText("")
            showToast("已清空扩展字段")
        }

        // 示例数据按钮
        binding.btnExampleExtension.setOnClickListener {
            insertExampleData()
        }
    }

    private fun initTextWatcher() {
        // 会话ID输入监听
        binding.edtConversationId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateUIStatus()
            }
        })

        // 扩展字段输入监听
        binding.edtLocalExtension.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateExtensionStatus()
                updateValidationStatus()
            }
        })
    }

    /**
     * 处理选择的会话
     */
    private fun handleSelectedConversation(conversationId: String, currentExtension: String?) {
        binding.edtConversationId.setText(conversationId)
        
        // 如果当前会话有扩展字段，则显示在输入框中
        if (!currentExtension.isNullOrEmpty()) {
            binding.edtLocalExtension.setText(currentExtension)
            showToast("已选择会话，当前扩展字段已加载")
        } else {
            showToast("已选择会话: $conversationId")
        }
    }

    /**
     * 格式化JSON内容
     */
    private fun formatJsonContent() {
        val content = binding.edtLocalExtension.text.toString().trim()
        if (content.isEmpty()) {
            showToast("请先输入JSON内容")
            return
        }

        try {
            val jsonObject = JSONObject(content)
            val formattedJson = jsonObject.toString(2) // 缩进2个空格
            binding.edtLocalExtension.setText(formattedJson)
            showToast("JSON格式化完成")
        } catch (e: JSONException) {
            showToast("JSON格式错误: ${e.message}")
        }
    }

    /**
     * 插入示例数据
     */
    private fun insertExampleData() {
        val exampleJson = """
{
  "example_key1": "example_value1",
  "example_key2": "example_value2"
}
        """.trimIndent()
        
        binding.edtLocalExtension.setText(exampleJson)
        showToast("已插入示例数据")
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
     * 更新扩展字段状态
     */
    private fun updateExtensionStatus() {
        val extension = getLocalExtension()
        
        // 更新长度显示
        binding.tvExtensionLength.text = "长度: ${extension.length}"
        
        // 更新预览
        updateExtensionPreview(extension)
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
     * 更新扩展字段预览
     */
    private fun updateExtensionPreview(extension: String) {
        if (extension.isEmpty()) {
            binding.tvExtensionPreview.text = "暂无扩展字段"
            return
        }

        // 尝试解析并格式化显示
        try {
            val jsonObject = JSONObject(extension)
            val formattedJson = jsonObject.toString(2)
            binding.tvExtensionPreview.text = "JSON格式:\n$formattedJson"
        } catch (e: JSONException) {
            // 不是JSON格式，直接显示原文
            val previewText = if (extension.length > 200) {
                "${extension.substring(0, 200)}..."
            } else {
                extension
            }
            binding.tvExtensionPreview.text = "原始格式:\n$previewText"
        }
    }

    /**
     * 更新验证状态
     */
    private fun updateValidationStatus() {
        val conversationId = getConversationId()
        val extension = getLocalExtension()
        
        when {
            conversationId.isEmpty() -> {
                binding.tvValidationStatus.text = "✗ 会话ID不能为空"
                binding.tvValidationStatus.setTextColor(resources.getColor(android.R.color.holo_red_dark))
            }
            !V2NIMConversationIdUtil.isConversationIdValid(conversationId) -> {
                binding.tvValidationStatus.text = "❌ 会话ID格式无效"
                binding.tvValidationStatus.setTextColor(resources.getColor(android.R.color.holo_red_dark))
            }
            extension.isNotEmpty() && !isValidJson(extension) -> {
                binding.tvValidationStatus.text = "⚠ 扩展字段不是有效的JSON格式"
                binding.tvValidationStatus.setTextColor(resources.getColor(android.R.color.holo_orange_dark))
            }
            else -> {
                val statusText = if (extension.isEmpty()) {
                    "✓ 参数验证通过，将清空扩展字段"
                } else {
                    "✓ 参数验证通过，扩展字段长度${extension.length}字符"
                }
                binding.tvValidationStatus.text = statusText
                binding.tvValidationStatus.setTextColor(resources.getColor(android.R.color.holo_green_dark))
            }
        }
    }

    /**
     * 检查是否为有效的JSON格式
     */
    private fun isValidJson(jsonString: String): Boolean {
        return try {
            JSONObject(jsonString)
            true
        } catch (e: JSONException) {
            false
        }
    }

    /**
     * 发起请求
     */
    override fun onRequest() {
        val conversationId = getConversationId()
        val localExtension = getLocalExtension()
        
        if (conversationId.isEmpty()) {
            showToast("请输入会话ID")
            return
        }

        if (!V2NIMConversationIdUtil.isConversationIdValid(conversationId)) {
            showToast("会话ID格式无效")
            return
        }

        // 显示确认对话框
        showUpdateConfirmation(conversationId, localExtension)
    }

    /**
     * 显示更新确认对话框
     */
    private fun showUpdateConfirmation(conversationId: String, localExtension: String) {
        val type = V2NIMConversationIdUtil.conversationType(conversationId)
        val targetId = V2NIMConversationIdUtil.conversationTargetId(conversationId)
        
        val operation = if (localExtension.isEmpty()) "清空扩展字段" else "更新扩展字段"
        val extensionInfo = if (localExtension.isEmpty()) {
            "将清空现有的扩展字段"
        } else {
            "扩展字段长度: ${localExtension.length}字符"
        }
        
        val messageText = "确定要${operation}吗？\n\n会话信息：\n类型：$type\n目标：$targetId\nID：$conversationId\n\n$extensionInfo"

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("确认${operation}")
            .setMessage(messageText)
            .setIcon(android.R.drawable.ic_menu_edit)
            .setPositiveButton("确定更新") { _, _ ->
                performUpdateOperation(conversationId, localExtension)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    /**
     * 执行更新操作
     */
    private fun performUpdateOperation(conversationId: String, localExtension: String) {
        activityViewModel.showLoadingDialog()
        
        // 传入空字符串表示清空扩展字段
        val extensionParam = localExtension.ifEmpty { null }
        
        NIMClient.getService(V2NIMLocalConversationService::class.java).updateConversationLocalExtension(
            conversationId,
            extensionParam,
            { updateResult(conversationId, localExtension, null) },
            { error -> updateResult(conversationId, localExtension, error) }
        )
    }

    /**
     * 更新结果
     */
    private fun updateResult(conversationId: String, localExtension: String, error: V2NIMError?) {
        activityViewModel.dismissLoadingDialog()
        
        if (error != null) {
            val resultText = buildFailureResultText(conversationId, localExtension, error)
            activityViewModel.refresh(resultText)
        } else {
            val resultText = buildSuccessResultText(conversationId, localExtension)
            activityViewModel.refresh(resultText)
            
            // 操作成功后清空输入框
            binding.edtConversationId.setText("")
            binding.edtLocalExtension.setText("")
        }
    }

    /**
     * 构建成功结果文本
     */
    private fun buildSuccessResultText(conversationId: String, localExtension: String): String {
        val sb = StringBuilder()
        val operation = if (localExtension.isEmpty()) "清空扩展字段" else "更新扩展字段"
        val type = V2NIMConversationIdUtil.conversationType(conversationId)
        val targetId = V2NIMConversationIdUtil.conversationTargetId(conversationId)
        
        sb.append("${operation}成功! 🎉\n\n")
        sb.append("操作详情:\n")
        sb.append("• 会话ID: $conversationId\n")
        sb.append("• 会话类型: $type\n")
        sb.append("• 目标ID: $targetId\n")
        sb.append("• 操作类型: $operation\n")
        
        if (localExtension.isEmpty()) {
            sb.append("• 扩展字段: 已清空\n\n")
            sb.append("✅ 清空效果:\n")
            sb.append("• 会话的本地扩展字段已被清空\n")
            sb.append("• 之前存储的自定义数据已删除\n")
        } else {
            sb.append("• 扩展字段长度: ${localExtension.length}字符\n\n")
            sb.append("✅ 更新效果:\n")
            sb.append("• 会话的本地扩展字段已更新\n")
            sb.append("• 新的自定义数据已保存到本地\n")
            
            // 显示扩展字段内容预览
            sb.append("\n📄 扩展字段内容:\n")
            if (isValidJson(localExtension)) {
                try {
                    val jsonObject = JSONObject(localExtension)
                    sb.append(jsonObject.toString(2))
                } catch (e: JSONException) {
                    sb.append(localExtension)
                }
            } else {
                val preview = if (localExtension.length > 300) {
                    "${localExtension.substring(0, 300)}..."
                } else {
                    localExtension
                }
                sb.append(preview)
            }
        }
        
        sb.append("\n\n💡 提示:\n")
        sb.append("• 本地扩展字段仅存储在本地设备\n")
        sb.append("• 不会同步到服务器或其他设备\n")
        sb.append("• 可用于存储自定义的会话标记或配置\n")
        
        return sb.toString()
    }

    /**
     * 构建失败结果文本
     */
    private fun buildFailureResultText(conversationId: String, localExtension: String, error: V2NIMError?): String {
        val sb = StringBuilder()
        val operation = if (localExtension.isEmpty()) "清空扩展字段" else "更新扩展字段"
        val type = V2NIMConversationIdUtil.conversationType(conversationId)
        val targetId = V2NIMConversationIdUtil.conversationTargetId(conversationId)
        
        sb.append("${operation}失败! ❌\n\n")
        sb.append("操作详情:\n")
        sb.append("• 会话ID: $conversationId\n")
        sb.append("• 会话类型: $type\n")
        sb.append("• 目标ID: $targetId\n")
        sb.append("• 操作类型: $operation\n")
        if (localExtension.isNotEmpty()) {
            sb.append("• 扩展字段长度: ${localExtension.length}字符\n")
        }
        sb.append("• 错误信息: ${error?.desc ?: "未知错误"}\n")
        sb.append("• 错误代码: ${error?.code ?: "N/A"}\n\n")
        
        sb.append("可能原因:\n")
        sb.append("• 会话不存在或已被删除\n")
        sb.append("• 网络连接问题\n")
        sb.append("• 用户未登录\n")
        sb.append("• 会话ID格式错误\n")
        sb.append("• 扩展字段内容过长\n")
        sb.append("• 服务异常\n\n")
        
        sb.append("解决建议:\n")
        sb.append("• 检查会话ID是否正确存在\n")
        sb.append("• 确认网络连接正常\n")
        sb.append("• 验证用户登录状态\n")
        sb.append("• 检查扩展字段内容和长度\n")
        sb.append("• 重试操作或联系技术支持\n")
        
        return sb.toString()
    }

    /**
     * 获取会话ID
     */
    private fun getConversationId(): String {
        return binding.edtConversationId.text.toString().trim()
    }

    /**
     * 获取本地扩展字段
     */
    private fun getLocalExtension(): String {
        return binding.edtLocalExtension.text.toString().trim()
    }
}