package com.netease.nim.samples.message.common

import androidx.lifecycle.MutableLiveData
import com.netease.nim.samples.base.list.viewmodels.BaseMultiSelectViewModel
import com.netease.nimlib.sdk.v2.conversation.model.V2NIMLocalConversation
import com.netease.nimlib.sdk.v2.message.V2NIMMessage

class V2MessageMultiSelectViewModel : BaseMultiSelectViewModel() {
    val selectListLiveData = MutableLiveData<List<V2NIMMessage>>()

    fun finishSelect(list: List<V2NIMMessage>){
        selectListLiveData.value = list
    }

    fun clearSelection() {
        selectListLiveData.value = emptyList()
    }
}