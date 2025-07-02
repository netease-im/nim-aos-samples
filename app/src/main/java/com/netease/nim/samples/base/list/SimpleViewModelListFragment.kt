package com.netease.nim.samples.base.list

import com.netease.nim.samples.base.list.model.ItemMultiSelectModel
import com.netease.nim.samples.base.list.viewmodels.BaseListViewModel


open class SimpleViewModelListFragment : BaseViewModelListFragment<BaseListViewModel>() {

    override fun setContentList(): List<ItemMultiSelectModel>? {
        return mutableListOf()
    }

    override fun createViewModel(): BaseListViewModel {
       return getFragmentViewModel(BaseListViewModel::class.java)
    }


}