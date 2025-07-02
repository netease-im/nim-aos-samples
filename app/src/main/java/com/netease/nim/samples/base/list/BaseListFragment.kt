package com.netease.nim.samples.base.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.netease.nim.nintest.base.fragment.BaseBindingFragment
import com.netease.nim.samples.base.list.model.ItemModel
import com.netease.nim.samples.base.list.viewholder.ContentHolder
import com.netease.nim.samples.base.list.viewholder.HeaderHolder
import com.netease.nim.samples.base.widget.recycleview.BaseRecycleAdapter
import com.netease.nim.samples.base.widget.recycleview.IRecycleAdapterViewHolder
import com.netease.nim.samples.base.widget.recycleview.OnRecycleViewItemClickListener
import com.netease.nim.samples.databinding.FragmentBaseListBinding

abstract class BaseListFragment : BaseBindingFragment<FragmentBaseListBinding>() {
    private var mContentList: List<ItemModel>? = null
    private lateinit var mAdapter: BaseRecycleAdapter<ItemModel>

    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentBaseListBinding {
        return FragmentBaseListBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.recycleView.layoutManager = layoutManager
        binding.recycleView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        mContentList = setContentList()
        mAdapter = getAdapter(mContentList)
        binding.recycleView.adapter = mAdapter
        binding.recycleView.addOnItemTouchListener(
            OnRecycleViewItemClickListener(context) { parent, view, position, id ->
                onRecycleViewItemClick(
                    position,
                    mContentList!![position]
                )
            }
        )
    }

    private fun getAdapter(contentList: List<ItemModel>?): BaseRecycleAdapter<ItemModel> {
        return object : BaseRecycleAdapter<ItemModel>(contentList) {
            override fun createRecycleAdapterItem(type: Int): IRecycleAdapterViewHolder<ItemModel> {
                return if (type == ItemModel.TYPE_HEADER) {
                    HeaderHolder()
                } else {
                    ContentHolder()
                }
            }
        }
    }


    fun updateContentList(list: List<ItemModel>?) {
        mContentList = list
        if(!isAdded)
        {
            return
        }
        mAdapter.data = list
        mAdapter.notifyDataSetChanged()
    }

    protected open fun onRecycleViewItemClick(position: Int, model: ItemModel) {}

    protected fun getContentList(): List<ItemModel> {
        return mAdapter.data
    }

    protected abstract fun setContentList(): List<ItemModel>?
}