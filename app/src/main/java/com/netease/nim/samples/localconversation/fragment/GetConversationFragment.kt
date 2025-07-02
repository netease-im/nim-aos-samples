package com.netease.nim.samples.localconversation.fragment

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.databinding.FragmentGetConversationBinding
import com.netease.nim.samples.localconversation.common.V2LocalConversationSelectViewModel
import com.netease.nim.samples.localconversation.common.V2NIMLocalConversationSelectFragment
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.V2NIMError
import com.netease.nimlib.sdk.v2.conversation.V2NIMLocalConversationService
import com.netease.nimlib.sdk.v2.conversation.model.V2NIMLocalConversation


class GetConversationFragment : BaseMethodExecuteFragment<FragmentGetConversationBinding>() {

    private lateinit var selectConversationViewModel: V2LocalConversationSelectViewModel
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        selectConversationViewModel = getActivityViewModel(V2LocalConversationSelectViewModel::class.java)
        selectConversationViewModel.selectItemLiveData.observe(viewLifecycleOwner, {
            activityViewModel.popFragment()
            binding.etConversationId.setText(it.conversationId)
        })
        binding.btnGetConversation.setOnClickListener {
            activityViewModel.addFragment(V2NIMLocalConversationSelectFragment.NAME)
        }
    }
    override fun onRequest() {

        // 获取输入参数
        val conversationId: String = binding.etConversationId.getText().toString()

        if (TextUtils.isEmpty(conversationId) ) {
            activityViewModel.refresh("请输入完整参数")
            return
        }
        activityViewModel.showLoadingDialog()
        // 调用API
        NIMClient.getService(V2NIMLocalConversationService::class.java)
            .getConversation(conversationId,
                { result -> // 更新UI
                    updateResult(null,result)
                }, { error -> // 错误处理
                    updateResult(error,null)
                })
    }

    private fun updateResult(error: V2NIMError?,conversation:V2NIMLocalConversation?){
        activityViewModel.dismissLoadingDialog()
        if(error != null){
            activityViewModel.refresh("获取失败: $error")
            return
        }
        val content = """
            获取成功,result:
                {
                    conversationId:${conversation!!.conversationId},
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
        activityViewModel.refresh(content)
    }

    /**
     * 创建ViewBinding，由子类实现
     */
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentGetConversationBinding {
        return FragmentGetConversationBinding.inflate(inflater, container, false)
    }

}