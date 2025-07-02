package com.netease.nim.samples.base.list.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.netease.nim.samples.base.list.model.ItemModel

open class BaseListViewModel : ViewModel() {

    private val itemList:MutableList<ItemModel> = mutableListOf()
    companion object {
        private const val TAG = "BaseFragmentListViewModel"
    }

    val listDataLiveData = MutableLiveData<MutableList<ItemModel>?>(mutableListOf())

    fun onAddEvent(event: String)
    {
        itemList.add(ItemModel(event));
        listDataLiveData.value = itemList
    }

    fun onAddEvent(event: ItemModel){
        itemList.add(event);
        listDataLiveData.value = itemList
    }

    fun onRefresh(list: List<ItemModel>?){
        list?.apply {
            itemList.clear()
            itemList.addAll(this)
            listDataLiveData.value = itemList
        }
    }

    fun onAppend(list: List<ItemModel>?){
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