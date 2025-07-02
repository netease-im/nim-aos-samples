package com.netease.nim.samples.friend

import androidx.lifecycle.MutableLiveData
import com.netease.nim.samples.base.list.viewmodels.BaseListViewModel
import com.netease.nimlib.sdk.v2.friend.V2NIMFriendAddApplication

class FriendApplicationSelectViewModel : BaseListViewModel() {
    val selectItemLiveData = MutableLiveData<V2NIMFriendAddApplication>()

    fun select(application: V2NIMFriendAddApplication){
        selectItemLiveData.value = application
    }
}