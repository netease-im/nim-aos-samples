package com.netease.nim.samples.message.common

import android.os.Bundle
import android.os.SystemClock
import android.view.View
import com.netease.nim.samples.base.list.BaseViewModelPageListFragment
import com.netease.nim.samples.base.list.model.ItemModel
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.auth.V2NIMLoginService
import com.netease.nimlib.sdk.v2.conversation.V2NIMLocalConversationService
import com.netease.nimlib.sdk.v2.message.V2NIMMessage
import com.netease.nimlib.sdk.v2.message.V2NIMMessageService
import com.netease.nimlib.sdk.v2.message.option.V2NIMMessageListOption

class V2NIMMessageSelectFragment : BaseViewModelPageListFragment<V2MessageSelectViewModel>() {
    companion object{
        const val NAME = "V2NIMMessageSelectFragment"
        private const val KEY_CONVERSATION_ID = "conversationId"

        fun newInstance(conversationId: String): V2NIMMessageSelectFragment {
            val fragment = V2NIMMessageSelectFragment()
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
    override fun getNextPage(listener: (errorCode: Int, result: List<ItemModel>?) -> Unit) {
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

    override fun setContentList(): List<ItemModel>? {
        return mutableListOf()
    }

    override fun createViewModel(): V2MessageSelectViewModel {
        return getActivityViewModel(V2MessageSelectViewModel::class.java)
    }

    override fun onRecycleViewItemClick(position: Int, model: ItemModel) {
        super.onRecycleViewItemClick(position, model)
        val castModel:V2MessageItemModel = model as V2MessageItemModel
        viewModel.select(castModel.message)

    }
}