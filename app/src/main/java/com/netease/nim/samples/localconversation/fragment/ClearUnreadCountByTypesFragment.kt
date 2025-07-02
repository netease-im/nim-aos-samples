package com.netease.nim.samples.localconversation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.databinding.FragmentClearUnreadCountByTypesBinding
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.V2NIMError
import com.netease.nimlib.sdk.v2.conversation.V2NIMLocalConversationService
import com.netease.nimlib.sdk.v2.conversation.enums.V2NIMConversationType

class ClearUnreadCountByTypesFragment : BaseMethodExecuteFragment<FragmentClearUnreadCountByTypesBinding>() {

    /**
     * 创建ViewBinding，由子类实现
     */
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentClearUnreadCountByTypesBinding {
        return FragmentClearUnreadCountByTypesBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initClickListeners()
        initCheckboxListeners()
        updateUIStatus()
    }

    private fun initClickListeners() {
        // 全选按钮
        binding.btnSelectAllTypes.setOnClickListener {
            binding.cbTypeP2p.isChecked = true
            binding.cbTypeTeam.isChecked = true
            binding.cbTypeSuperTeam.isChecked = true
            showToast("已选择所有会话类型")
        }

        // 全不选按钮
        binding.btnClearAllTypes.setOnClickListener {
            binding.cbTypeP2p.isChecked = false
            binding.cbTypeTeam.isChecked = false
            binding.cbTypeSuperTeam.isChecked = false
            showToast("已清空所有选择")
        }

        // 常用类型按钮
        binding.btnSelectCommonTypes.setOnClickListener {
            binding.cbTypeP2p.isChecked = true
            binding.cbTypeTeam.isChecked = true
            binding.cbTypeSuperTeam.isChecked = false
            showToast("已选择常用类型（单聊和群聊）")
        }
    }

    private fun initCheckboxListeners() {
        val checkboxes = listOf(
            binding.cbTypeP2p,
            binding.cbTypeTeam,
            binding.cbTypeSuperTeam
        )

        checkboxes.forEach { checkbox ->
            checkbox.setOnCheckedChangeListener { _, _ ->
                updateUIStatus()
            }
        }
    }

    /**
     * 更新UI状态
     */
    private fun updateUIStatus() {
        val selectedTypes = getSelectedConversationTypes()

        // 更新选择数量显示
        binding.tvSelectedTypesCount.text = "当前选择类型数量: ${selectedTypes.size}个"

        // 更新选择预览
        updateSelectedTypesPreview(selectedTypes)

        // 更新影响范围描述
        updateImpactDescription(selectedTypes)

    }

    /**
     * 更新选择预览
     */
    private fun updateSelectedTypesPreview(selectedTypes: List<V2NIMConversationType>) {
        if (selectedTypes.isEmpty()) {
            binding.tvSelectedTypesPreview.text = "暂未选择任何会话类型"
            return
        }

        val typeNames = selectedTypes.map { getConversationTypeName(it) }
        binding.tvSelectedTypesPreview.text = "已选择: ${typeNames.joinToString("、")}"
    }

    /**
     * 更新影响范围描述
     */
    private fun updateImpactDescription(selectedTypes: List<V2NIMConversationType>) {
        if (selectedTypes.isEmpty()) {
            return
        }

        val sb = StringBuilder()
        sb.append("此操作将清空以下类型的所有会话未读数:\n")

        selectedTypes.forEach { type ->
            when (type) {
                V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P -> {
                    sb.append("• 单聊: 所有一对一私聊会话\n")
                }
                V2NIMConversationType.V2NIM_CONVERSATION_TYPE_TEAM -> {
                    sb.append("• 群聊: 所有普通群聊会话\n")
                }
                V2NIMConversationType.V2NIM_CONVERSATION_TYPE_SUPER_TEAM -> {
                    sb.append("• 超大群: 所有超大群会话\n")
                }
                else -> {
                    sb.append("• ${getConversationTypeName(type)}\n")
                }
            }
        }

    }


    /**
     * 发起请求
     */
    override fun onRequest() {
        val selectedTypes = getSelectedConversationTypes()

        if (selectedTypes.isEmpty()) {
            showToast("请至少选择一种会话类型")
            return
        }


        activityViewModel.showLoadingDialog()
        NIMClient.getService(V2NIMLocalConversationService::class.java).clearUnreadCountByTypes(
            selectedTypes,
            { updateResult(true, null) },
            { error -> updateResult(false, error) }
        )
    }

    /**
     * 更新结果
     */
    private fun updateResult(success: Boolean, error: V2NIMError?) {
        activityViewModel.dismissLoadingDialog()

        if (error != null) {
            activityViewModel.refresh("根据会话类型清空未读数失败: $error")
            return
        }

        if (success) {
            
            val resultText = buildResultText()
            activityViewModel.refresh(resultText)
        }
    }

    /**
     * 构建结果文本
     */
    private fun buildResultText(): String {
        val selectedTypes = getSelectedConversationTypes()
        val sb = StringBuilder()

        sb.append("根据会话类型清空未读数操作成功:\n")
        sb.append("操作时间: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date())}\n")
        sb.append("清空的会话类型数量: ${selectedTypes.size}种\n")

        sb.append("\n清空的会话类型:\n")
        selectedTypes.forEachIndexed { index, type ->
            sb.append("${index + 1}. ${getConversationTypeName(type)} (${type.name})\n")
        }

        sb.append("\n操作详情:\n")
        selectedTypes.forEach { type ->
            when (type) {
                V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P -> {
                    sb.append("• 单聊: 所有一对一私聊会话的未读数已清零\n")
                }
                V2NIMConversationType.V2NIM_CONVERSATION_TYPE_TEAM -> {
                    sb.append("• 群聊: 所有普通群聊会话的未读数已清零\n")
                }
                V2NIMConversationType.V2NIM_CONVERSATION_TYPE_SUPER_TEAM -> {
                    sb.append("• 超大群: 所有超大群会话的未读数已清零\n")
                }
                else -> {
                    sb.append("• ${getConversationTypeName(type)}: 未读数已清零\n")
                }
            }
        }

        return sb.toString()
    }

    /**
     * 获取选择的会话类型列表
     */
    private fun getSelectedConversationTypes(): List<V2NIMConversationType> {
        val selectedTypes = mutableListOf<V2NIMConversationType>()

        if (binding.cbTypeP2p.isChecked) {
            selectedTypes.add(V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P)
        }
        if (binding.cbTypeTeam.isChecked) {
            selectedTypes.add(V2NIMConversationType.V2NIM_CONVERSATION_TYPE_TEAM)
        }
        if (binding.cbTypeSuperTeam.isChecked) {
            selectedTypes.add(V2NIMConversationType.V2NIM_CONVERSATION_TYPE_SUPER_TEAM)
        }

        return selectedTypes
    }

    /**
     * 获取会话类型的显示名称
     */
    private fun getConversationTypeName(type: V2NIMConversationType): String {
        return when (type) {
            V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P -> "单聊"
            V2NIMConversationType.V2NIM_CONVERSATION_TYPE_TEAM -> "群聊"
            V2NIMConversationType.V2NIM_CONVERSATION_TYPE_SUPER_TEAM -> "超大群"
            V2NIMConversationType.V2NIM_CONVERSATION_TYPE_UNKNOWN -> "未知类型"
            else -> "其他类型"
        }
    }
}