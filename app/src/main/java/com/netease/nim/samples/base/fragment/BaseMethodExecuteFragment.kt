package com.netease.nim.nintest.base.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.viewbinding.ViewBinding
import com.netease.nim.samples.base.viewmodels.BaseMethodExecuteRequestViewModel
import com.netease.nim.samples.base.viewmodels.BaseMethodExecuteActivityViewModel

abstract class BaseMethodExecuteFragment<T : ViewBinding> : BaseBindingFragment<T>()
{
    lateinit var requestViewModel: BaseMethodExecuteRequestViewModel
    lateinit var activityViewModel: BaseMethodExecuteActivityViewModel

    abstract fun onRequest()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        requestViewModel = getFragmentViewModel(BaseMethodExecuteRequestViewModel::class.java)
        requestViewModel.requestLiveData.observe(viewLifecycleOwner, Observer {
            if(it == true){
                onRequest()
            }
        })
        activityViewModel = getActivityViewModel(BaseMethodExecuteActivityViewModel::class.java)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

}