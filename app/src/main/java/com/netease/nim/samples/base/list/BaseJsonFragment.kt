package com.netease.nim.samples.base.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.netease.nim.nintest.base.fragment.BaseBindingFragment
import com.netease.nim.samples.base.list.model.ItemMultiSelectModel
import com.netease.nim.samples.base.list.viewholder.MultiSelectHolder
import com.netease.nim.samples.base.widget.recycleview.BaseRecycleAdapter
import com.netease.nim.samples.base.widget.recycleview.IRecycleAdapterViewHolder
import com.netease.nim.samples.base.widget.recycleview.OnRecycleViewItemClickListener
import com.netease.nim.samples.databinding.FragmentBaseJsonBinding
import java.lang.Boolean
import kotlin.Int
import kotlin.String

abstract class BaseJsonFragment<T : ItemMultiSelectModel> : BaseBindingFragment<FragmentBaseJsonBinding>() {

    private var mContentList: List<ItemMultiSelectModel>? = null
    private lateinit var mSelectList: MutableList<ItemMultiSelectModel>
    private var mAdapter: BaseRecycleAdapter<ItemMultiSelectModel>? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.etJson.setText(initJson())
        binding.btnRequest.setOnClickListener {
            val json = binding.etJson.text.toString().replace("\u00A0", " ");
//            if(json.isEmpty()){
//                showToast("请输入正确的入参json")
//                return@setOnClickListener
//            }
            onRequest(json)
        }
        binding.btnClear.setOnClickListener {
            updateResult(mutableListOf())
        }

        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.recycleView.layoutManager = layoutManager
        binding.recycleView.isNestedScrollingEnabled = false
        binding.recycleView.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )
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
    }

    private fun getAdapter(contentList: List<ItemMultiSelectModel>?): BaseRecycleAdapter<ItemMultiSelectModel> {
        return object : BaseRecycleAdapter<ItemMultiSelectModel>(contentList) {
            override fun createRecycleAdapterItem(type: Int): IRecycleAdapterViewHolder<ItemMultiSelectModel> {
                return MultiSelectHolder()
            }
        }
    }

    private fun updateContentList(list: List<ItemMultiSelectModel>?) {
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
    val selectList: List<ItemMultiSelectModel>
        get() = mSelectList

    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentBaseJsonBinding {
        return FragmentBaseJsonBinding.inflate(inflater, container, false)
    }

    protected fun updateResult(resultList: List<T>){
        updateContentList(resultList)
    }

    protected fun updateResult(result: String){
        val list: MutableList<ItemMultiSelectModel> = mutableListOf();
        list.add(ItemMultiSelectModel(result))
        updateContentList(list)
    }

    abstract fun initJson(): String

    abstract fun onRequest(json:String)



}