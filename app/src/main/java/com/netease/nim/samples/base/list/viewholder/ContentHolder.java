package com.netease.nim.samples.base.list.viewholder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import com.netease.nim.samples.R;
import com.netease.nim.samples.base.list.model.ItemModel;
import com.netease.nim.samples.base.widget.recycleview.IRecycleAdapterViewHolder;


/**
 * @author jintao02
 * @date 2021/7/26 15:13
 */
public class ContentHolder implements IRecycleAdapterViewHolder<ItemModel>
{
	private TextView mTextView;
	@Override
	public int getLayoutResId()
	{
		return R.layout.item_content_layout;
	}
	
	@Override
	public void bindViews(@NonNull View root)
	{
		mTextView = root.findViewById(R.id.item_content_tv);
	}
	
	@Override
	public void setData(@NonNull ItemModel model)
	{
		mTextView.setText(model.getContent());
		mTextView.setEnabled(model.isSupport());
	}
}