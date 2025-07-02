package com.netease.nim.samples.message.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.common.getV2NIMMessageJson
import com.netease.nim.samples.common.getV2NIMMessageListJson
import com.netease.nim.samples.databinding.FragmentGetMessageListBinding
import com.netease.nim.samples.localconversation.common.V2LocalConversationSelectViewModel
import com.netease.nim.samples.localconversation.common.V2NIMLocalConversationSelectFragment
import com.netease.nim.samples.message.common.V2MessageSelectViewModel
import com.netease.nim.samples.message.common.V2NIMMessageSelectFragment
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.V2NIMError
import com.netease.nimlib.sdk.v2.message.V2NIMMessage
import com.netease.nimlib.sdk.v2.message.V2NIMMessageService
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageQueryDirection
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageType
import com.netease.nimlib.sdk.v2.message.option.V2NIMMessageListOption
import com.netease.nimlib.sdk.v2.message.result.V2NIMMessageListResult

class GetMessageListExFragment : BaseMethodExecuteFragment<FragmentGetMessageListBinding>() {
    /**
     * 锚点消息
     */
    private var anchorMessage: V2NIMMessage? = null

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
    ): FragmentGetMessageListBinding {
        return FragmentGetMessageListBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //选择会话
        binding.btnGetConversation.setOnClickListener {
            activityViewModel.addFragment(V2NIMLocalConversationSelectFragment.NAME)
        }
        //选择锚点消息
        binding.btnSelectAnchorMsg.setOnClickListener {
            val conversationId = binding.etConversationId.text.toString()
            if (conversationId.isNotEmpty()) {
                activityViewModel.addFragment(V2NIMMessageSelectFragment.NAME, conversationId)
            } else {
                showToast("请输入会话ID")
            }
        }

        selectConversationViewModel =
            getActivityViewModel(V2LocalConversationSelectViewModel::class.java)
        selectConversationViewModel.selectItemLiveData.observe(viewLifecycleOwner, {
            activityViewModel.popFragment()
            binding.etConversationId.setText(it.conversationId)
        })

        selectMessageViewModel = getActivityViewModel(V2MessageSelectViewModel::class.java)
        selectMessageViewModel.selectItemLiveData.observe(viewLifecycleOwner, {
            activityViewModel.popFragment()
            anchorMessage = it
            binding.tvAnchorMsg.text = getV2NIMMessageJson(it)
        })

    }


    /**
     * 发起请求
     */
    override fun onRequest() {
        val conversationId = binding.etConversationId.text.toString()
        if (conversationId.isEmpty()) {
            showToast("请先输入会话ID")
            return
        }

        val option = generateMessageListOption(conversationId)
        activityViewModel.showLoadingDialog()
        NIMClient.getService(V2NIMMessageService::class.java).getMessageListEx(option,
            { result -> updateResult(result, null) },
            { error -> updateResult(null, error) })

    }

    /**
     * 更新结果
     */
    private fun updateResult(result: V2NIMMessageListResult?, error: V2NIMError?) {
        activityViewModel.dismissLoadingDialog()
        if (error != null) {
            activityViewModel.refresh("查询消息失败: $error")
            return
        }

        activityViewModel.refresh("查询消息成功: ${getV2NIMMessageListResult(result)}")
    }

    private fun getV2NIMMessageListResult(result: V2NIMMessageListResult?): String {
        if (result == null) {
            return "null"
        }
        return """{
            messages = ${getV2NIMMessageListJson(result.messages)},
            anchorMessage = ${getV2NIMMessageJson(result.anchorMessage)}
            }
        """.trimIndent()
    }

    /**
     * 生成消息查询选项参数
     */
    private fun generateMessageListOption(conversationId: String): V2NIMMessageListOption {
        val builder = V2NIMMessageListOption.V2NIMMessageListOptionBuilder.builder(conversationId)

        // 消息类型筛选
        val messageTypes = getSelectedMessageTypes()
        if (messageTypes.isNotEmpty()) {
            builder.withMessageTypes(messageTypes)
        }

        // 开始时间
        val beginTimeStr = binding.edtBeginTime.text.toString()
        if (beginTimeStr.isNotEmpty()) {
            try {
                builder.withBeginTime(beginTimeStr.toLong())
            } catch (e: NumberFormatException) {
                // 忽略格式错误
            }
        }

        // 结束时间
        val endTimeStr = binding.edtEndTime.text.toString()
        if (endTimeStr.isNotEmpty()) {
            try {
                builder.withEndTime(endTimeStr.toLong())
            } catch (e: NumberFormatException) {
                // 忽略格式错误
            }
        }

        // 查询条数
        val limitStr = binding.edtLimit.text.toString()
        if (limitStr.isNotEmpty()) {
            try {
                val limit = limitStr.toInt()
                if (limit > 0 && limit <= 100) {
                    builder.withLimit(limit)
                }
            } catch (e: NumberFormatException) {
                // 忽略格式错误
            }
        }

        // 锚点消息
        if (anchorMessage != null) {
            builder.withAnchorMessage(anchorMessage)
        }

        // 查询方向
        val direction = if (binding.rbDesc.isChecked) {
            V2NIMMessageQueryDirection.V2NIM_QUERY_DIRECTION_DESC
        } else {
            V2NIMMessageQueryDirection.V2NIM_QUERY_DIRECTION_ASC
        }
        builder.withDirection(direction)

        // 严格模式
        builder.withStrictMode(binding.cbStrictMode.isChecked)

        // 仅查询本地
        builder.withOnlyQueryLocal(binding.cbOnlyQueryLocal.isChecked)


        return builder.build()
    }

    /**
     * 获取选中的消息类型列表
     */
    private fun getSelectedMessageTypes(): List<V2NIMMessageType> {
        val types = mutableListOf<V2NIMMessageType>()

        if (binding.cbTypeText.isChecked) {
            types.add(V2NIMMessageType.V2NIM_MESSAGE_TYPE_TEXT)
        }
        if (binding.cbTypeImage.isChecked) {
            types.add(V2NIMMessageType.V2NIM_MESSAGE_TYPE_IMAGE)
        }
        if (binding.cbTypeAudio.isChecked) {
            types.add(V2NIMMessageType.V2NIM_MESSAGE_TYPE_AUDIO)
        }
        if (binding.cbTypeVideo.isChecked) {
            types.add(V2NIMMessageType.V2NIM_MESSAGE_TYPE_VIDEO)
        }
        if (binding.cbTypeFile.isChecked) {
            types.add(V2NIMMessageType.V2NIM_MESSAGE_TYPE_FILE)
        }
        if (binding.cbTypeLocation.isChecked) {
            types.add(V2NIMMessageType.V2NIM_MESSAGE_TYPE_LOCATION)
        }
        if (binding.cbTypeCustom.isChecked) {
            types.add(V2NIMMessageType.V2NIM_MESSAGE_TYPE_CUSTOM)
        }

        return types
    }
}