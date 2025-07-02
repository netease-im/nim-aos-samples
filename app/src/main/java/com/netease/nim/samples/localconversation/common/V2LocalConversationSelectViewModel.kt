package com.netease.nim.samples.localconversation.common

import androidx.lifecycle.MutableLiveData
import com.netease.nim.samples.base.list.viewmodels.BaseListViewModel
import com.netease.nimlib.sdk.v2.conversation.model.V2NIMLocalConversation

class V2LocalConversationSelectViewModel : BaseListViewModel() {
    val selectItemLiveData = MutableLiveData<V2NIMLocalConversation>()

    fun select(conversation: V2NIMLocalConversation){
        selectItemLiveData.value = conversation
    }
}