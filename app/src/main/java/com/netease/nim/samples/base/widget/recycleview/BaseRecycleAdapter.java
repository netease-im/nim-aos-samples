package com.netease.nim.samples.base.widget.recycleview;

import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jintao02
 * @date 2021/7/26 14:36
 */
public abstract class BaseRecycleAdapter<T extends IRecycleAdapterItem> extends RecyclerView.Adapter<RecyclerViewViewHolder<T>> implements IRecycleAdapter<T>
{
	private List<? extends T> mItemModels;
	public BaseRecycleAdapter(List<? extends T> itemModels)
	{
		setData(itemModels);
	}
	
	
	@NonNull
	@Override
	public RecyclerViewViewHolder<T> onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
	{
		return new RecyclerViewViewHolder<>(parent,createRecycleAdapterItem(viewType));
		
	}
	
	@Override
	public void onBindViewHolder(@NonNull RecyclerViewViewHolder<T> viewHolder, int position)
	{
		IRecycleAdapterViewHolder<T> item = viewHolder.getItemHolder();
		if(item == null)
		{
			return;
		}
		T itemModel = mItemModels.get(position);
		if(itemModel == null)
		{
			return;
		}
		item.setData(itemModel);
	}
	
	
	@Override
	public int getItemViewType(int position)
	{
		return mItemModels.get(position).getItemType();
	}
	
	@Override
	public int getItemCount()
	{
		return mItemModels.size();
	}
	
	
	public void setData(List<? extends T> itemModels)
	{
		if(itemModels == null)
		{
			itemModels = new ArrayList<>();
		}
		mItemModels = itemModels;
	}
	
	
	public List<? extends T> getData()
	{
		return mItemModels;
	}
	
}
