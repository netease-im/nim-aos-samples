package com.netease.nim.samples.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.netease.nim.samples.utils.LogUtils

/**
 * 通过Fragment对象的hashcode判断position位置上的Fragment是否发生了变化，
 */
class MainViewPageAdapter(fragmentActivity: FragmentActivity, private val fragmentList: List<Fragment>,private val fragmentHashCodeList: List<Long>) : FragmentStateAdapter(fragmentActivity)
{
	companion object
	{
		private const val TAG = "MainViewPageAdapter"
	}
	
	
	/**
	 * 通过Fragment对象的hashcode表示ItemId
	 */
	override fun getItemId(position: Int): Long
	{
		val hashCode = fragmentHashCodeList[position]
		LogUtils.d(TAG,"getItemId($position) = $hashCode")
		return hashCode
	}
	
	/**
	 * 判断当前是否包括该[itemId]对应的Fragment
	 */
	override fun containsItem(itemId: Long): Boolean
	{
		val contains = fragmentHashCodeList.contains(itemId)
		LogUtils.d(TAG,"containsItem($itemId) = $contains")
		return contains
	}
	
	override fun getItemCount(): Int
	{
		return fragmentList.size
	}
	
	override fun createFragment(position: Int): Fragment
	{
		val fragment = fragmentList[position]
		LogUtils.d(TAG,"createFragment($position) = $fragment")
		return fragment
	}
	
	
	
	
}