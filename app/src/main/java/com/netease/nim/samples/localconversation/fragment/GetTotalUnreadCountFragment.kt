package com.netease.nim.samples.localconversation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.databinding.FragmentGetTotalUnreadCountBinding
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.conversation.V2NIMLocalConversationService

class GetTotalUnreadCountFragment : BaseMethodExecuteFragment<FragmentGetTotalUnreadCountBinding>() {


    /**
     * 创建ViewBinding，由子类实现
     */
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentGetTotalUnreadCountBinding {
        return FragmentGetTotalUnreadCountBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }



    /**
     * 发起请求
     */
    override fun onRequest() {
        refreshUnreadCount()
    }

    /**
     * 刷新未读数
     */
    private fun refreshUnreadCount() {
        try {
            val unreadCount = NIMClient.getService(V2NIMLocalConversationService::class.java).totalUnreadCount
            updateResult(unreadCount)
        } catch (e: Exception) {
            updateResult(-1, e)
        }
    }


    /**
     * 更新结果
     */
    private fun updateResult(unreadCount: Int, error: Exception? = null) {

        val resultText = if (error != null)  buildFailureResultText(error) else buildSuccessResultText(unreadCount)
        activityViewModel.refresh(resultText)

    }

    /**
     * 构建成功结果文本
     */
    private fun buildSuccessResultText(unreadCount: Int): String {
        val sb = StringBuilder()
        
        sb.append("获取总未读数成功! 📊\n\n")
        sb.append("查询结果:\n")
        sb.append("• 总未读数: $unreadCount\n")
        
        return sb.toString()
    }

    /**
     * 构建失败结果文本
     */
    private fun buildFailureResultText(error: Exception): String {
        val sb = StringBuilder()
        
        sb.append("获取总未读数失败! ❌\n\n")
        sb.append("错误信息:\n")
        sb.append("• 错误类型: ${error.javaClass.simpleName}\n")
        sb.append("• 错误描述: ${error.message ?: "未知错误"}\n")
        
        return sb.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}