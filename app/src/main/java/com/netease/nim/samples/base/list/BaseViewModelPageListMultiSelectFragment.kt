package com.netease.nim.samples.base.list

import android.os.Bundle
import android.view.View
import com.netease.nim.samples.base.list.model.ItemModel
import com.netease.nim.samples.base.list.model.ItemMultiSelectModel
import com.netease.nim.samples.base.list.viewmodels.BaseListViewModel
import com.netease.nim.samples.base.list.viewmodels.BaseMultiSelectViewModel

abstract class BaseViewModelPageListMultiSelectFragment<T : BaseMultiSelectViewModel> :
    BaseViewModelMultiSelectFragment<T>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnNextPage.visibility = View.VISIBLE
        binding.btnNextPage.setOnClickListener {
            getNextPage()
        }
    }

    protected fun getNextPage() {
        showLoadingDialog()
        getNextPage { errorCode, result ->
            dismissLoadingDialog()
            if (errorCode != 200) {
                showToast("获取数据失败,errorCode = $errorCode")
                return@getNextPage
            }

            if (result.isNullOrEmpty()) {
                showToast("没有数据了")
            } else {
                viewModel.onAppend(result)
            }
        }
    }

    abstract fun getNextPage(listener: (errorCode: Int, result: List<ItemMultiSelectModel>?) -> Unit)

}