package com.netease.nim.samples.localconversation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.databinding.FragmentRemoveConversationListenerBinding
import com.netease.nim.samples.localconversation.common.GlobalConversationListenerManager

class RemoveConversationListenerFragment : BaseMethodExecuteFragment<FragmentRemoveConversationListenerBinding>() {

    /**
     * 创建ViewBinding，由子类实现
     */
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentRemoveConversationListenerBinding {
        return FragmentRemoveConversationListenerBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateUI()
    }


    /**
     * 移除会话监听器
     */
    private fun removeConversationListener() {
        if (!GlobalConversationListenerManager.isListenerAdded()) {
            showToast("没有监听器需要移除")
            updateOperationResult("❌ 操作失败: 当前没有活跃的监听器")
            return
        }

        // 执行移除操作
        val success = GlobalConversationListenerManager.removeListener()

        if (success) {
            updateOperationResult("✅ 监听器移除成功! 已停止接收所有会话事件回调")
            showToast("✅ 监听器移除成功")
        } else {
            updateOperationResult("❌ 移除监听器失败，可能监听器实例不匹配或已被移除")
            showToast("❌ 移除监听器失败")
        }

        // 刷新UI状态
        updateUI()
    }

    /**
     * 更新UI显示
     */
    private fun updateUI() {
        updateListenerStatus()
    }

    /**
     * 更新监听器状态显示
     */
    private fun updateListenerStatus() {
        if (GlobalConversationListenerManager.isListenerAdded()) {
            binding.tvListenerStatus.text = "● 监听器已添加 (可以移除)"
            binding.tvListenerStatus.setTextColor(resources.getColor(android.R.color.holo_green_dark))
        } else {
            binding.tvListenerStatus.text = "● 没有活跃的监听器"
            binding.tvListenerStatus.setTextColor(resources.getColor(android.R.color.holo_red_dark))
        }
    }


    /**
     * 更新操作结果显示
     */
    private fun updateOperationResult(message: String) {
        binding.tvOperationResult.text = message
        val color = if (message.contains("✅")) {
            resources.getColor(android.R.color.holo_green_dark)
        } else {
            resources.getColor(android.R.color.holo_red_dark)
        }
        binding.tvOperationResult.setTextColor(color)
    }

    /**
     * 重写onRequest方法，这里主要用于显示当前状态和执行移除操作
     */
    override fun onRequest() {

        val isListenerActive = GlobalConversationListenerManager.isListenerAdded()

        val statusText = buildString {
            append("移除会话监听器:\n\n")

            if (isListenerActive) {

                removeConversationListener()

                append("✅ 发现活跃监听器:\n")
                append("• 类型: V2NIMLocalConversationListener\n")
                append("• 状态: 全局监听中\n")
                append("• 可以执行移除操作\n\n")

                append("执行移除:\n")
                append("• removeConversationListener: 移除指定监听器\n")
                append("• 移除后立即停止接收所有回调事件\n")

            } else {
                append("ℹ️ 当前状态:\n")
                append("• 没有活跃的会话监听器\n")
                append("• 无需执行移除操作\n")
                append("• 如需监听会话事件，请先添加监听器\n")
            }
        }
        
        activityViewModel.refresh(statusText)
    }
}