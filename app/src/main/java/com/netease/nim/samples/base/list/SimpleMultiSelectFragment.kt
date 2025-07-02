package com.netease.nim.samples.base.list

import com.netease.nim.samples.base.list.BaseMultiSelectFragment
import com.netease.nim.samples.base.list.model.ItemMultiSelectModel


open class SimpleMultiSelectFragment : BaseMultiSelectFragment() {

    override fun setContentList(): List<ItemMultiSelectModel>? {
        return mutableListOf()
    }

}