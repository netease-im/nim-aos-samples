package com.netease.nim.samples.message.common

import android.os.Bundle
import android.view.View
import com.netease.nim.samples.base.list.BaseViewModelPageListMultiSelectFragment
import com.netease.nim.samples.base.list.model.ItemMultiSelectModel
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.message.V2NIMMessage
import com.netease.nimlib.sdk.v2.message.V2NIMMessageService
import com.netease.nimlib.sdk.v2.message.option.V2NIMMessageListOption

class V2NIMMessageMultiSelectFragment : BaseViewModelPageListMultiSelectFragment<V2MessageMultiSelectViewModel>() {
    companion object{
        const val NAME = "V2NIMMessageMultiSelectFragment"
        private const val KEY_CONVERSATION_ID = "conversationId"

        fun newInstance(conversationId: String): V2NIMMessageMultiSelectFragment {
            val fragment = V2NIMMessageMultiSelectFragment()
            val bundle = Bundle()
            bundle.putString(KEY_CONVERSATION_ID, conversationId)
            fragment.arguments = bundle
            return fragment
        }
    }

    private val limit = 50
    private val beginTime = 0L
    private var endTime = 0L
    private var anchor: V2NIMMessage? = null
    private var conversationId:String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        conversationId = arguments?.getString(KEY_CONVERSATION_ID)
        getNextPage()
    }
    override fun getNextPage(listener: (errorCode: Int, result: List<ItemMultiSelectModel>?) -> Unit) {
        val option = V2NIMMessageListOption.V2NIMMessageListOptionBuilder.builder(conversationId)
            .withBeginTime(beginTime)
            .withEndTime(endTime)
            .withAnchorMessage(anchor)
            .withLimit(limit)
            .withOnlyQueryLocal(true)
            .build()
        NIMClient.getService(V2NIMMessageService::class.java).getMessageList(option, { result -> // 更新UI
            if(result.isNotEmpty()){
                anchor = result[result.size-1]
                anchor?.apply {
                    endTime = this.createTime
                }
            }
            listener(200, result.map { V2MessageItemModel(it) })
        }, { error -> // 错误处理
            listener(error.code, null)
        })
    }

    override fun setContentList(): List<ItemMultiSelectModel>? {
        return mutableListOf()
    }

    override fun createViewModel(): V2MessageMultiSelectViewModel {
        return getActivityViewModel(V2MessageMultiSelectViewModel::class.java)
    }

    override fun onFinishSelect(list: List<ItemMultiSelectModel>) {
        val selectList = list.map {
            val model: V2MessageItemModel = it as V2MessageItemModel
            model.message
        }
        viewModel.finishSelect(selectList)
    }
}