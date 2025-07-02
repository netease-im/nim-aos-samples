package com.netease.nim.samples.base.widget.recycleview;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author jintao02
 * @date 2021/7/26 14:34
 */
class RecyclerViewViewHolder<T> extends RecyclerView.ViewHolder
{
	private IRecycleAdapterViewHolder<T> mItemHolder;
	RecyclerViewViewHolder(@NonNull ViewGroup parent, IRecycleAdapterViewHolder<T> itemHolder)
	{
		super(LayoutInflater.from(parent.getContext()).inflate(itemHolder.getLayoutResId(), parent,false));
		mItemHolder = itemHolder;
		mItemHolder.bindViews(itemView);
	}
	
	IRecycleAdapterViewHolder<T> getItemHolder()
	{
		return mItemHolder;
	}
}