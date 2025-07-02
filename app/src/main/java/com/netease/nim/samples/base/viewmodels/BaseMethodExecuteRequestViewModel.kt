package com.netease.nim.samples.base.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BaseMethodExecuteRequestViewModel : ViewModel() {

    companion object {
        private const val TAG = "BaseMethodExecuteRequestViewModel"
    }

    val requestLiveData = MutableLiveData<Boolean?>(null)


    fun request(){
        requestLiveData.value = true
    }

    fun cancel()
    {
        requestLiveData.value = false
    }
}