package com.netease.nim.samples.localconversation.fragment

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.base.list.model.ItemModel
import com.netease.nim.samples.databinding.FragmentGetConversationListBinding
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.V2NIMError
import com.netease.nimlib.sdk.v2.conversation.V2NIMLocalConversationService
import com.netease.nimlib.sdk.v2.conversation.result.V2NIMLocalConversationResult


class GetConversationListFragment : BaseMethodExecuteFragment<FragmentGetConversationListBinding>() {
    override fun onRequest() {

        // 获取输入参数
        val offsetStr: String = binding.etOffset.getText().toString()
        val limitStr: String = binding.etLimit.getText().toString()

        if (TextUtils.isEmpty(offsetStr) || TextUtils.isEmpty(limitStr)) {
            activityViewModel.refresh("请输入完整参数")
            return
        }

        val offset = offsetStr.toLong()
        val limit = limitStr.toInt()
        activityViewModel.showLoadingDialog()
        // 调用API
        NIMClient.getService(V2NIMLocalConversationService::class.java)
            .getConversationList(offset, limit,
                { result -> // 更新UI
                    updateResult(null,result)
                }, { error -> // 错误处理
                    updateResult(error,null)
                })
    }

    private fun updateResult(error:V2NIMError?,result:V2NIMLocalConversationResult?){
        activityViewModel.dismissLoadingDialog()
        if(error != null){
            activityViewModel.refresh("获取失败: $error")
            return
        }
        val resultList = mutableListOf<ItemModel>()
        resultList.add(ItemModel("获取成功:offset:${result!!.offset},isFinished:${result.isFinished},result size:${result.conversationList.size}"))
        var content = ""
        for (conversation in result.conversationList) {
            content = """
                {
                    conversationId:${conversation.conversationId},
                    type:${conversation.type},
                    name:${conversation.name},
                    avatar:${conversation.avatar},
                    unreadCount:${conversation.unreadCount},
                    isMute:${conversation.isMute},
                    isStickTop:${conversation.isStickTop},
                    localExtension:${conversation.localExtension},
                    updateTime:${conversation.updateTime},
                    lastMessage:{ 
                                    senderName:${conversation.lastMessage?.senderName},
                                    messageType:${conversation.lastMessage?.messageType},
                                    text:${conversation.lastMessage?.text} 
                    },
                    sortOrder:${conversation.sortOrder}
                }
            """.trimIndent()
            val item = ItemModel(content,null)
            resultList.add(item)
        }
        activityViewModel.refresh(resultList)
    }

    /**
     * 创建ViewBinding，由子类实现
     */
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentGetConversationListBinding {
        return FragmentGetConversationListBinding.inflate(inflater, container, false)
    }

}