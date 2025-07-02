package com.netease.nim.samples.base.list.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.netease.nim.samples.base.list.model.ItemModel
import com.netease.nim.samples.base.list.model.ItemMultiSelectModel

open class BaseMultiSelectViewModel : ViewModel() {

    private val itemList:MutableList<ItemMultiSelectModel> = mutableListOf()
    companion object {
        private const val TAG = "BaseFragmentListViewModel"
    }

    val listDataLiveData = MutableLiveData<MutableList<ItemMultiSelectModel>?>(mutableListOf())

    fun onAddEvent(event: String)
    {
        itemList.add(ItemMultiSelectModel(event));
        listDataLiveData.value = itemList
    }

    fun onAddEvent(event: ItemMultiSelectModel){
        itemList.add(event);
        listDataLiveData.value = itemList
    }

    fun onAppend(list: List<ItemMultiSelectModel>?){
        list?.apply {
            itemList.addAll(this)
            listDataLiveData.value = itemList
        }
    }

    fun clear()
    {
        itemList.clear()
        listDataLiveData.value = itemList
    }
}