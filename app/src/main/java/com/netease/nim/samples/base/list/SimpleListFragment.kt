package com.netease.nim.samples.base.list

import com.netease.nim.samples.base.list.BaseListFragment
import com.netease.nim.samples.base.list.model.ItemModel

open class SimpleListFragment : BaseListFragment() {
    override fun setContentList(): List<ItemModel>? {
       return mutableListOf()
    }
}