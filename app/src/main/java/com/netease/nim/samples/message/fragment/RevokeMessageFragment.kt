package com.netease.nim.samples.message.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.common.getV2NIMMessageJson
import com.netease.nim.samples.databinding.FragmentRevokeMessageBinding
import com.netease.nim.samples.localconversation.common.V2LocalConversationSelectViewModel
import com.netease.nim.samples.localconversation.common.V2NIMLocalConversationSelectFragment
import com.netease.nim.samples.message.common.V2MessageSelectViewModel
import com.netease.nim.samples.message.common.V2NIMMessageSelectFragment
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.V2NIMError
import com.netease.nimlib.sdk.v2.message.V2NIMMessage
import com.netease.nimlib.sdk.v2.message.V2NIMMessageService
import com.netease.nimlib.sdk.v2.message.params.V2NIMMessageRevokeParams

class RevokeMessageFragment : BaseMethodExecuteFragment<FragmentRevokeMessageBinding>() {
    /**
     * 需要撤回的消息
     */
    private var revokeMsg: V2NIMMessage? = null

    /**
     * 选择会话的 ViewModel
     */
    private lateinit var selectConversationViewModel: V2LocalConversationSelectViewModel
    /**
     * 选择消息的 ViewModel
     */
    private lateinit var selectMessageViewModel: V2MessageSelectViewModel


    /**
     * 创建ViewBinding，由子类实现
     */
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentRevokeMessageBinding {
        return FragmentRevokeMessageBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //选择会话
        binding.btnGetConversation.setOnClickListener {
            activityViewModel.addFragment(V2NIMLocalConversationSelectFragment.NAME)
        }
        //选择消息
        binding.btnSelectMsg.setOnClickListener {
            val conversationId = binding.etConversationId.text.toString()
            if (conversationId.isNotEmpty()) {
                activityViewModel.addFragment(V2NIMMessageSelectFragment.NAME,conversationId)
            }else{
                showToast("请输入会话ID")
            }
        }

        binding.cbRevokeParams.setOnCheckedChangeListener { _, isChecked ->
            binding.llRevokeParams.visibility = if(isChecked) View.VISIBLE else View.GONE
        }

        selectConversationViewModel = getActivityViewModel(V2LocalConversationSelectViewModel::class.java)
        selectConversationViewModel.selectItemLiveData.observe(viewLifecycleOwner, {
            activityViewModel.popFragment()
            binding.etConversationId.setText(it.conversationId)
        })

        selectMessageViewModel = getActivityViewModel(V2MessageSelectViewModel::class.java)
        selectMessageViewModel.selectItemLiveData.observe(viewLifecycleOwner, {
            activityViewModel.popFragment()
            revokeMsg = it
            binding.tvReplyMsg.text = getV2NIMMessageJson(it)
        })

    }


    /**
     * 发起请求
     */
    override fun onRequest() {
        if (revokeMsg == null){
            showToast("请先选择需要撤回的消息")
            return
        }

        val params: V2NIMMessageRevokeParams? = generateParams()
        activityViewModel.showLoadingDialog()
        NIMClient.getService(V2NIMMessageService::class.java).revokeMessage(revokeMsg,params,
            { updateResult(null) },
            { error -> updateResult(error) })

    }

    /**
     * 更新结果
     */
    private fun updateResult(error: V2NIMError?){
        activityViewModel.dismissLoadingDialog()
        if(error != null){
            activityViewModel.refresh("撤回失败: $error")
            return
        }
        activityViewModel.refresh("撤回成功")
    }

    /**
     * 生成参数
     */
    private fun generateParams(): V2NIMMessageRevokeParams? {

        if(!binding.cbRevokeParams.isChecked){
            return null
        }

        return V2NIMMessageRevokeParams.V2NIMMessageRevokeParamsBuilder.builder()
            .withPostscript(binding.edtPostscript.text.toString().takeIf { it.isNotEmpty() })
            .withExtension(binding.edtServerExtension.text.toString().takeIf { it.isNotEmpty() })
            .withPushContent(binding.edtPushContent.text.toString().takeIf { it.isNotEmpty() })
            .withPushPayload(binding.edtPushPayload.text.toString().takeIf { it.isNotEmpty() })
            .withEnv(binding.edtEnv.text.toString().takeIf { it.isNotEmpty() })
            .build()
    }

}
