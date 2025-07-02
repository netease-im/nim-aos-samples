package com.netease.nim.samples.base.list.viewholder;

import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.netease.nim.samples.R;
import com.netease.nim.samples.base.list.model.ItemMultiSelectModel;
import com.netease.nim.samples.base.widget.recycleview.IRecycleAdapterViewHolder;

/**
 * @author jintao02
 * @date 2021/7/26 15:13
 */
public class MultiSelectHolder implements IRecycleAdapterViewHolder<ItemMultiSelectModel>
{
	private CheckBox mCheckBox;
	private TextView mTextView;
	@Override
	public int getLayoutResId()
	{
		return R.layout.item_multi_select_layout;
	}
	
	@Override
	public void bindViews(@NonNull View root)
	{
		mCheckBox = root.findViewById(R.id.item_select);
		mTextView = root.findViewById(R.id.item_content_tv);
	}
	
	@Override
	public void setData(@NonNull ItemMultiSelectModel model)
	{
		Boolean selected = model.isSelected();
		if(selected == null){
			mCheckBox.setVisibility(View.GONE);
		}else
		{
			mCheckBox.setVisibility(View.VISIBLE);
			mCheckBox.setChecked(selected);
		}

		mTextView.setText(model.getContent());
		mTextView.setEnabled(model.isSupport());
	}
}