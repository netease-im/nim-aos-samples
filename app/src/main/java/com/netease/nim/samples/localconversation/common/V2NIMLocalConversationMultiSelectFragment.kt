package com.netease.nim.samples.localconversation.common

import android.os.Bundle
import android.view.View
import com.netease.nim.samples.base.list.BaseViewModelPageListMultiSelectFragment
import com.netease.nim.samples.base.list.model.ItemMultiSelectModel
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.conversation.V2NIMLocalConversationService

class V2NIMLocalConversationMultiSelectFragment : BaseViewModelPageListMultiSelectFragment<V2LocalConversationMultiSelectViewModel>() {
    companion object{
        const val NAME = "V2NIMLocalConversationMultiSelectFragment"
    }

    private val limit = 100
    private var offset = 0L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getNextPage()
    }
    override fun getNextPage(listener: (errorCode: Int, result: List<ItemMultiSelectModel>?) -> Unit) {
        NIMClient.getService(V2NIMLocalConversationService::class.java)
            .getConversationList(offset, limit, { result -> // 更新UI
                offset = result.offset
                listener(200, result.conversationList.map {
                    // 5. 使用字符串模板和多行字符串提高可读性
                    V2LocalConversationItemModel(it)
                })
            }, { error -> // 错误处理
                listener(error.code, null)
            })
    }

    override fun setContentList(): List<ItemMultiSelectModel>? {
        return mutableListOf()
    }

    override fun createViewModel(): V2LocalConversationMultiSelectViewModel {
        return getActivityViewModel(V2LocalConversationMultiSelectViewModel::class.java)
    }

    override fun onFinishSelect(list: List<ItemMultiSelectModel>) {
        val conversationList = list.map {
            val model: V2LocalConversationItemModel = it as V2LocalConversationItemModel
            model.conversation
        }
        viewModel.finishSelect(conversationList)
    }
}