package com.netease.nim.samples.base.widget.recycleview;

import androidx.annotation.NonNull;

/**
 * Adapter统一接口
 * @author jintao02
 * @date 2021/7/26 14:27
 */
public interface IRecycleAdapter<T>
{
	/**
	 * 根据类型创建ViewHolder
	 * @param type
	 * @return
	 */
	@NonNull
	IRecycleAdapterViewHolder<T> createRecycleAdapterItem(int type);
}
