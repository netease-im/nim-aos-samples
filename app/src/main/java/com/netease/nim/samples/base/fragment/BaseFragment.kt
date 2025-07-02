package com.netease.nim.nintest.base.fragment

import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.netease.nim.samples.base.widget.LoadingDialog

/**
 * Fragment的基类
 */
open class BaseFragment : Fragment()
{
    private var mActivityProvider: ViewModelProvider? = null
    private var mFragmentProvider: ViewModelProvider? = null
    private var mAppFactory: ViewModelProvider.Factory? = null

    private var mLoadingDialog: LoadingDialog? = null

    protected open fun showToast(p: Int) {
        Toast.makeText(context, p, Toast.LENGTH_LONG).show()
    }

    protected open fun showToast(message: String?) {
        if(context == null){
            return
        }
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    protected open fun isLoadingDialogShowing(): Boolean {
        return mLoadingDialog != null && mLoadingDialog!!.isShowing
    }

    protected open fun showLoadingDialog() {
        try {
            if (mLoadingDialog == null) {
                mLoadingDialog = LoadingDialog()
                mLoadingDialog!!.show(parentFragmentManager)
            } else if (!isLoadingDialogShowing()) {
                mLoadingDialog!!.show(parentFragmentManager)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    protected open fun dismissLoadingDialog() {
        try {
            if (isLoadingDialogShowing()) {
                mLoadingDialog!!.dismiss()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            mLoadingDialog = null
        }
    }
    
    /**
     * 获取Activity级别的ViewModel实例
     */
    protected open fun <T : ViewModel> getActivityViewModel(modelClass: Class<T>) : T
    {
        if(mActivityProvider == null)
        {
            mActivityProvider = ViewModelProvider(requireActivity(),ViewModelProvider.NewInstanceFactory())
        }
        return mActivityProvider!!.get(modelClass)
    }
    
    /**
     * 获取Fragment级别的ViewModel实例
     */
    open fun <T : ViewModel> getFragmentViewModel(modelClass: Class<T>) : T
    {
        if(mFragmentProvider == null)
        {
            mFragmentProvider = ViewModelProvider(this,ViewModelProvider.NewInstanceFactory())
        }
        return mFragmentProvider!!.get(modelClass)
    }

//    /**
//     * 获取全局ViewModel实例
//     */
//    protected open fun <T : ViewModel> getAppViewModel(modelClass: Class<T>) : T
//    {
//        //这里不能去缓存该ViewModelProvider,因为该ViewModelProvider引用了Application，生命周期比Activity要长
//        return ViewModelProvider(requireActivity().application,getAppFactory()).get(modelClass)
//    }
//
//    private fun getAppFactory() : ViewModelProvider.Factory
//    {
//        if(mAppFactory == null)
//        {
//            mAppFactory = ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
//        }
//        return mAppFactory!!
//    }

}