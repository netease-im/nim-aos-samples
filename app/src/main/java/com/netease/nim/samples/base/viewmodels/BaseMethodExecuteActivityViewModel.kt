package com.netease.nim.samples.base.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.netease.nim.samples.base.list.model.ItemModel
import com.netease.nim.samples.base.model.AddFragmentModel

class BaseMethodExecuteActivityViewModel : ViewModel() {

    private val resultList:MutableList<ItemModel> = mutableListOf()
    companion object {
        private const val TAG = "BaseMethodExecuteActivityViewModel"
    }

    val resultListLiveData = MutableLiveData<MutableList<ItemModel>?>(mutableListOf())
    val addFragmentLiveData = MutableLiveData<AddFragmentModel?>()
    val showLoadingDialogLiveData = MutableLiveData<Boolean>(false)


    fun refresh(list: List<ItemModel>?){
        list?.apply {
            resultList.clear()
            resultList.addAll(this)
            resultListLiveData.value = resultList
        }
    }

    fun refresh(result: String){
        resultList.clear()
        resultList.add(ItemModel(result))
        resultListLiveData.value = resultList
    }

    fun addFragment(fragment: String){
        addFragmentLiveData.value = AddFragmentModel(fragment,null)
    }

    fun addFragment(fragment: String,param:Any?){
        val params = arrayOf(param)
        addFragmentLiveData.value = AddFragmentModel(fragment, params)
    }

    fun addFragment(fragment: String,params: Array<Any?>?){
        addFragmentLiveData.value = AddFragmentModel(fragment,params)
    }

    fun popFragment(){
        addFragmentLiveData.value = null
    }

    fun showLoadingDialog(){
        showLoadingDialogLiveData.value = true
    }

    fun dismissLoadingDialog(){
        showLoadingDialogLiveData.value = false
    }


    fun clear()
    {
        resultList.clear()
        resultListLiveData.value = resultList
    }
}