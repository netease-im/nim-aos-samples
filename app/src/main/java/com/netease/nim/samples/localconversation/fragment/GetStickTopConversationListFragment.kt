package com.netease.nim.samples.localconversation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.databinding.FragmentGetStickTopConversationListBinding
import com.netease.nim.samples.localconversation.common.V2LocalConversationSelectViewModel
import com.netease.nim.samples.localconversation.common.V2NIMLocalConversationSelectFragment
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.V2NIMError
import com.netease.nimlib.sdk.v2.conversation.V2NIMLocalConversationService
import com.netease.nimlib.sdk.v2.conversation.enums.V2NIMConversationType
import com.netease.nimlib.sdk.v2.conversation.model.V2NIMLastMessage
import com.netease.nimlib.sdk.v2.conversation.model.V2NIMLocalConversation
import java.text.SimpleDateFormat
import java.util.*

class GetStickTopConversationListFragment : BaseMethodExecuteFragment<FragmentGetStickTopConversationListBinding>() {

    /**
     * 创建ViewBinding，由子类实现
     */
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentGetStickTopConversationListBinding {
        return FragmentGetStickTopConversationListBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    /**
     * 发起请求
     */
    override fun onRequest() {
        activityViewModel.showLoadingDialog()
        NIMClient.getService(V2NIMLocalConversationService::class.java).getStickTopConversationList(
            { conversationList -> updateResult(conversationList, null) },
            { error -> updateResult(null, error) }
        )
    }

    /**
     * 更新结果
     */
    private fun updateResult(conversationList: List<V2NIMLocalConversation>?, error: V2NIMError?) {
        activityViewModel.dismissLoadingDialog()
        
        if (error != null) {
            activityViewModel.refresh("查询置顶会话列表失败: $error")
            return
        }

        conversationList?.let {
            val resultText = buildResultText(it)
            activityViewModel.refresh(resultText)
        }
    }



    /**
     * 构建结果文本
     */
    private fun buildResultText(conversationList: List<V2NIMLocalConversation>): String {
        val sb = StringBuilder()
        
        sb.append("查询置顶会话列表成功:\n")
        sb.append("置顶会话总数: ${conversationList.size}\n")
        
        if (conversationList.isEmpty()) {
            sb.append("当前没有置顶的会话\n")
            sb.append("\n提示: 可以先置顶一些会话，然后再查询")
            return sb.toString()
        }

        sb.append("置顶会话详情:\n")
        conversationList.forEachIndexed { index, conversation ->
            sb.append("${index + 1}. 会话信息:\n")
            sb.append("   conversationId: ${conversation.conversationId}\n")
            sb.append("   type: ${conversation.type}\n")
            sb.append("   name: ${conversation.name}\n")
            sb.append("   avatar: ${conversation.avatar}\n")
            sb.append("   createTime: ${formatTime(conversation.createTime)}\n")
            sb.append("   updateTime: ${formatTime(conversation.updateTime)}\n")
            sb.append("   localExtension: ${conversation.localExtension}\n")
            sb.append("   lastMessage: ${getLastMessageInfo(conversation.lastMessage)}\n")
            sb.append("   unreadCount: ${conversation.unreadCount}\n")
            sb.append("   mute: ${conversation.isMute}\n")
            sb.append("   stickTop: ${conversation.isStickTop}\n")
            
            if (index < conversationList.size - 1) {
                sb.append("\n")
            }
        }

        return sb.toString()
    }

    /**
     * 获取最后一条消息信息
     */
    private fun getLastMessageInfo(lastMessage: V2NIMLastMessage?): String {
        return if (lastMessage == null) {
            "null"
        } else {
            "{messageType: ${lastMessage.messageType}, text: ${lastMessage.text}, senderName: ${lastMessage.senderName}, messageRefer: ${lastMessage.messageRefer?.messageClientId}}"
        }
    }

    /**
     * 格式化时间
     */
    private fun formatTime(timestamp: Long): String {
        if (timestamp <= 0) return "0"
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return "${dateFormat.format(Date(timestamp))} ($timestamp)"
    }
}