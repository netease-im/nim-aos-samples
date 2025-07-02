package com.netease.nim.samples.localconversation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.databinding.FragmentClearTotalUnreadCountBinding
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.V2NIMError
import com.netease.nimlib.sdk.v2.conversation.V2NIMLocalConversationService

class ClearTotalUnreadCountFragment : BaseMethodExecuteFragment<FragmentClearTotalUnreadCountBinding>() {

    /**
     * 创建ViewBinding，由子类实现
     */
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentClearTotalUnreadCountBinding {
        return FragmentClearTotalUnreadCountBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initClickListeners()
        refreshCurrentUnreadCount()
    }

    private fun initClickListeners() {
        // 查看当前未读数按钮
        binding.btnCheckCurrentUnread.setOnClickListener {
            refreshCurrentUnreadCount()
        }

        // 刷新状态按钮
        binding.btnRefreshStatus.setOnClickListener {
            refreshCurrentUnreadCount()
            showToast("状态已刷新")
        }
    }


    /**
     * 刷新当前未读数
     */
    private fun refreshCurrentUnreadCount() {
        val totalUnreadCount =
            NIMClient.getService(V2NIMLocalConversationService::class.java).getTotalUnreadCount()
        binding.tvCurrentUnreadCount.text = "当前总未读数: $totalUnreadCount"
    }

    /**
     * 发起请求
     */
    override fun onRequest() {
        // 记录清空前的未读数
        val currentUnreadText = binding.tvCurrentUnreadCount.text.toString()

        activityViewModel.showLoadingDialog()
        NIMClient.getService(V2NIMLocalConversationService::class.java).clearTotalUnreadCount(
            { updateResult(currentUnreadText, null) },
            { error -> updateResult(currentUnreadText, error) }
        )
    }

    /**
     * 更新结果
     */
    private fun updateResult(beforeClearText: String, error: V2NIMError?) {
        activityViewModel.dismissLoadingDialog()

        if (error != null) {
            activityViewModel.refresh("清空本地会话未读数失败: $error")
            return
        }

        // 清空成功后刷新状态
        refreshCurrentUnreadCount()

        val resultText = buildResultText(beforeClearText)
        activityViewModel.refresh(resultText)
    }

    /**
     * 构建结果文本
     */
    private fun buildResultText(beforeClearText: String): String {
        val sb = StringBuilder()

        sb.append("清空本地会话未读数成功:\n")
        sb.append("清空前状态: $beforeClearText\n")
        sb.append("清空后状态: 所有会话未读数已重置为0\n")

        return sb.toString()
    }
}