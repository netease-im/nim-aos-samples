package com.netease.nim.nintest.base.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import androidx.lifecycle.ViewModel

/**
 * 使用一个ViewModel的，支持ViewBinding的Fragment的基类
 */
abstract class BaseViewModelBindingFragment<T1 : ViewModel,T2 : ViewBinding> : BaseBindingFragment<T2>()
{
    private lateinit var mPriViewModel: T1
    
    val viewModel: T1
        get()
        {
            return mPriViewModel
        }
    
    
    abstract fun createViewModel(): T1
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        mPriViewModel = createViewModel()
        return super.onCreateView(inflater, container, savedInstanceState)
    }

}