package com.netease.nim.samples.friend

import android.os.Bundle
import android.view.View
import com.netease.nim.samples.base.list.BaseViewModelPageListFragment
import com.netease.nim.samples.base.list.model.ItemModel
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.v2.friend.V2NIMFriendService
import com.netease.nimlib.sdk.v2.friend.option.V2NIMFriendAddApplicationQueryOption

class FriendApplicationSelectFragment : BaseViewModelPageListFragment<FriendApplicationSelectViewModel>() {
    companion object{
        const val NAME = "FriendApplicationSelectFragment"
    }
    private val limit = 100
    private var offset = 0L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getNextPage()
    }
    override fun getNextPage(listener: (errorCode: Int, result: List<ItemModel>?) -> Unit) {
        val option = V2NIMFriendAddApplicationQueryOption
            .V2NIMFriendAddApplicationQueryOptionBuilder.builder().withOffset(offset)
            .withLimit(limit).build()
        NIMClient.getService(V2NIMFriendService::class.java)
            .getAddApplicationList(option, { result -> // 更新UI
                offset = result.offset
                listener(200, result.getInfos().map {
                    // 5. 使用字符串模板和多行字符串提高可读性
                    FriendApplicationItemModel(it)
                })
            }, { error -> // 错误处理
                listener(error.code, null)
            })
    }

    override fun setContentList(): List<ItemModel>? {
        return mutableListOf()
    }

    override fun createViewModel(): FriendApplicationSelectViewModel {
        return getActivityViewModel(FriendApplicationSelectViewModel::class.java)
    }

    override fun onRecycleViewItemClick(position: Int, model: ItemModel) {
        super.onRecycleViewItemClick(position, model)
        val applicationModel:FriendApplicationItemModel = model as
                FriendApplicationItemModel
        viewModel.select(applicationModel.application)

    }
}