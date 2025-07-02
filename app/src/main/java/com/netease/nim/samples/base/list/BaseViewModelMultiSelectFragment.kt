package com.netease.nim.samples.base.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.netease.nim.nintest.base.fragment.BaseViewModelBindingFragment
import com.netease.nim.samples.base.list.model.ItemMultiSelectModel
import com.netease.nim.samples.base.list.viewholder.MultiSelectHolder
import com.netease.nim.samples.base.list.viewmodels.BaseMultiSelectViewModel
import com.netease.nim.samples.base.widget.recycleview.BaseRecycleAdapter
import com.netease.nim.samples.base.widget.recycleview.IRecycleAdapterViewHolder
import com.netease.nim.samples.base.widget.recycleview.OnRecycleViewItemClickListener
import com.netease.nim.samples.databinding.FragmentBaseListBinding
import java.lang.Boolean
import kotlin.Int

abstract class BaseViewModelMultiSelectFragment<T: BaseMultiSelectViewModel> : BaseViewModelBindingFragment<T, FragmentBaseListBinding>() {
    private var mContentList: List<ItemMultiSelectModel>? = null
    private lateinit var mSelectList: MutableList<ItemMultiSelectModel>
    private var mAdapter: BaseRecycleAdapter<ItemMultiSelectModel>? = null

    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentBaseListBinding {
        return FragmentBaseListBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rlTitle.visibility = View.VISIBLE
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.recycleView.layoutManager = layoutManager
        binding.recycleView.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )
        mContentList = setContentList()
        mSelectList = ArrayList()
        mContentList?.forEach {
            if (Boolean.TRUE == it.isSelected) {
                mSelectList.add(it)
            }
        }
        mAdapter = getAdapter(mContentList)
        binding.recycleView.adapter = mAdapter
        binding.recycleView.addOnItemTouchListener(
            OnRecycleViewItemClickListener(context,
                AdapterView.OnItemClickListener { parent, view, position, id ->
                    val itemMultiSelectModel = mContentList!![position]
                    val selected = itemMultiSelectModel.isSelected ?: return@OnItemClickListener
                    itemMultiSelectModel.isSelected = !selected
                    if (itemMultiSelectModel.isSelected) {
                        if (!mSelectList.contains(itemMultiSelectModel)) {
                            mSelectList.add(itemMultiSelectModel)
                        }
                    } else {
                        mSelectList.remove(itemMultiSelectModel)
                    }
                    mAdapter!!.notifyDataSetChanged()
                })
        )

        binding.btnFinish.setOnClickListener {
            onFinishSelect(mSelectList)
        }

        viewModel.listDataLiveData.observe(this.viewLifecycleOwner, Observer {
            mContentList = it
            mAdapter!!.data = it
            mAdapter!!.notifyDataSetChanged()
        })

    }

    private fun getAdapter(contentList: List<ItemMultiSelectModel>?): BaseRecycleAdapter<ItemMultiSelectModel> {
        return object : BaseRecycleAdapter<ItemMultiSelectModel>(contentList) {
            override fun createRecycleAdapterItem(type: Int): IRecycleAdapterViewHolder<ItemMultiSelectModel> {
                return MultiSelectHolder()
            }
        }
    }

    protected abstract fun onFinishSelect(list: List<ItemMultiSelectModel>)

    protected abstract fun setContentList(): List<ItemMultiSelectModel>?
    fun updateContentList(list: List<ItemMultiSelectModel>?) {
        mContentList = list
        if(!isAdded)
        {
            return
        }
        mAdapter!!.data = list
        mAdapter!!.notifyDataSetChanged()
    }

    val contentList: List<ItemMultiSelectModel>
        get() = mAdapter!!.data
    val selectList: List<ItemMultiSelectModel>?
        get() = mSelectList
}