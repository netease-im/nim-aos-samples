package com.netease.nim.nintest.base.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

/**
 * 使用ViewDataBinding方式的Fragment基类
 */
abstract class BaseBindingFragment<T : ViewBinding> : BaseFragment()
{
    /**
     * private ViewBingding，不暴露给外部
     */
    private var mPriBinding: T? = null
    /**
     * 对子类暴露的ViewBinding
     */
    protected val binding:T
        get()
        {
            return mPriBinding!!
        }
    
    
    /**
     * 创建ViewBinding，由子类实现
     */
    abstract fun createViewBinding(inflater: LayoutInflater,
                                   container: ViewGroup?): T
    
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mPriBinding = createViewBinding(inflater,container)
        return mPriBinding?.root
    }

    
    override fun onDestroyView()
    {
        super.onDestroyView()
        //为防止内存泄漏，需要在destory的时候将mPriBinding置为null
        mPriBinding = null
    }
}