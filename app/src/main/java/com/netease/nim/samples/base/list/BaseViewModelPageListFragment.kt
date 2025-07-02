package com.netease.nim.samples.base.list

import android.os.Bundle
import android.view.View
import com.netease.nim.samples.base.list.model.ItemModel
import com.netease.nim.samples.base.list.viewmodels.BaseListViewModel

abstract class BaseViewModelPageListFragment<T : BaseListViewModel> :
    BaseViewModelListFragment<T>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnNextPage.visibility = View.VISIBLE
        viewModel.clear()
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

    abstract fun getNextPage(listener: (errorCode: Int, result: List<ItemModel>?) -> Unit)

}