package com.netease.nim.samples.base

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.netease.nim.nintest.base.fragment.BaseMethodExecuteFragment
import com.netease.nim.samples.R
import com.netease.nim.samples.base.list.model.ItemModel
import com.netease.nim.samples.base.list.viewholder.ContentHolder
import com.netease.nim.samples.base.viewmodels.BaseMethodExecuteActivityViewModel
import com.netease.nim.samples.base.widget.recycleview.BaseRecycleAdapter
import com.netease.nim.samples.base.widget.recycleview.IRecycleAdapterViewHolder
import com.netease.nim.samples.databinding.ActivityBaseMethodExecuteBinding

abstract class BaseMethodExecuteActivity: BaseActivity(){

    companion object {
        const val KEY_METHOD_NAME = "method_name"

        @JvmStatic
        inline fun <reified T : BaseMethodExecuteActivity> startActivity(context: Context, methodName: String) {
            val intent = Intent(context, T::class.java)
            intent.putExtra(KEY_METHOD_NAME, methodName)
            context.startActivity(intent)
        }
    }

    private lateinit var binding: ActivityBaseMethodExecuteBinding

    private lateinit var activityViewModel:BaseMethodExecuteActivityViewModel

    private var currentFragment:BaseMethodExecuteFragment<*>? = null

    private var mAdapter: BaseRecycleAdapter<ItemModel>? = null

    protected var methodName: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        methodName = intent.getStringExtra(KEY_METHOD_NAME)
        binding = ActivityBaseMethodExecuteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.tvTitle.text = methodName
        activityViewModel = ViewModelProvider(this)[BaseMethodExecuteActivityViewModel::class.java]
        activityViewModel.resultListLiveData.observe(this) {
            updateResult(it)
        }
        activityViewModel.addFragmentLiveData.observe(this) {
            if (it == null) {
                if (supportFragmentManager.backStackEntryCount > 0) {
                    supportFragmentManager.popBackStack()
                }
            } else {
                addFragment(it.fragment,it.params)
            }

        }
        activityViewModel.showLoadingDialogLiveData.observe(this) {
            if (it) {
                showLoadingDialog()
            } else {
                dismissLoadingDialog()
            }
        }
        currentFragment = initFragment()
        currentFragment?.apply {
            addRequestFragment(this)
        }
        binding.btnRequest.setOnClickListener {
            currentFragment?.requestViewModel?.request()
        }
        binding.btnClear.setOnClickListener {
            activityViewModel.clear()
        }

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recycleView.layoutManager = layoutManager
        binding.recycleView.isNestedScrollingEnabled = false
        binding.recycleView.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )
        mAdapter = getAdapter(null)
        binding.recycleView.adapter = mAdapter
    }

    private fun getAdapter(contentList: List<ItemModel>?): BaseRecycleAdapter<ItemModel> {
        return object : BaseRecycleAdapter<ItemModel>(contentList) {
            override fun createRecycleAdapterItem(type: Int): IRecycleAdapterViewHolder<ItemModel> {
                return ContentHolder()
            }
        }
    }

    private fun updateContentList(list: List<ItemModel>?) {
        mAdapter!!.data = list
        mAdapter!!.notifyDataSetChanged()
    }

    protected fun updateResult(resultList: List<ItemModel>?){
        updateContentList(resultList)
    }

    abstract fun initFragment(): BaseMethodExecuteFragment<*>?

    private fun addRequestFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.request_fragment_container, fragment)
            .commit()
    }

    fun addFragment(fragment: Fragment,name:String) {
        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, fragment)
            .addToBackStack(name)
            .commit()
    }

    open fun addFragment(fragment: String,params: Array<Any?>?){

    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            activityViewModel.popFragment()
        } else {
            super.onBackPressed()
        }
    }
}