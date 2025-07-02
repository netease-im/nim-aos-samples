package com.netease.nim.samples.localconversation.common

import android.os.Bundle
import android.view.View
import com.netease.nim.samples.base.list.BaseViewModelPageListFragment
import com.netease.nim.samples.base.list.model.ItemModel
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.conversation.V2NIMLocalConversationService

class V2NIMLocalConversationSelectFragment : BaseViewModelPageListFragment<V2LocalConversationSelectViewModel>() {
    companion object{
        const val NAME = "V2NIMLocalConversationSelectFragment"
    }
    private val limit = 100
    private var offset = 0L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getNextPage()
    }
    override fun getNextPage(listener: (errorCode: Int, result: List<ItemModel>?) -> Unit) {
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

    override fun setContentList(): List<ItemModel>? {
        return mutableListOf()
    }

    override fun createViewModel(): V2LocalConversationSelectViewModel {
        return getActivityViewModel(V2LocalConversationSelectViewModel::class.java)
    }

    override fun onRecycleViewItemClick(position: Int, model: ItemModel) {
        super.onRecycleViewItemClick(position, model)
        val conversationModel:V2LocalConversationItemModel = model as V2LocalConversationItemModel
        viewModel.select(conversationModel.conversation)

    }
}