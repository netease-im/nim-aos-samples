package com.netease.nim.samples.message.common

import androidx.lifecycle.MutableLiveData
import com.netease.nim.samples.base.list.viewmodels.BaseListViewModel
import com.netease.nimlib.sdk.v2.conversation.model.V2NIMLocalConversation
import com.netease.nimlib.sdk.v2.message.V2NIMMessage

class V2MessageSelectViewModel : BaseListViewModel() {
    val selectItemLiveData = MutableLiveData<V2NIMMessage>()

    fun select(value: V2NIMMessage){
        selectItemLiveData.value = value
    }
}