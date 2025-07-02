package com.netease.nim.samples.localconversation.common

import androidx.lifecycle.MutableLiveData
import com.netease.nim.samples.base.list.viewmodels.BaseMultiSelectViewModel
import com.netease.nimlib.sdk.v2.conversation.model.V2NIMLocalConversation

class V2LocalConversationMultiSelectViewModel : BaseMultiSelectViewModel() {
    val selectListLiveData = MutableLiveData<List<V2NIMLocalConversation>>()

    fun finishSelect(list: List<V2NIMLocalConversation>){
        selectListLiveData.value = list
    }
}