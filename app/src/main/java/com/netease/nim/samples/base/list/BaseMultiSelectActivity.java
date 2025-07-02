package com.netease.nim.samples.base.list;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.nim.samples.R;
import com.netease.nim.samples.base.BaseActivity;
import com.netease.nim.samples.base.list.model.ItemMultiSelectModel;
import com.netease.nim.samples.base.list.viewholder.MultiSelectHolder;
import com.netease.nim.samples.base.widget.recycleview.BaseRecycleAdapter;
import com.netease.nim.samples.base.widget.recycleview.IRecycleAdapterViewHolder;
import com.netease.nim.samples.base.widget.recycleview.OnRecycleViewItemClickListener;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseMultiSelectActivity extends BaseActivity
{
	protected RecyclerView               mRecyclerView;
	private   List<ItemMultiSelectModel> mContentList;
	private List<ItemMultiSelectModel>               mSelectList;
	private BaseRecycleAdapter<ItemMultiSelectModel> mAdapter;
	private Button                                   mFinishBtn;
	private   TextView                                 mTitleTv;
	private   RelativeLayout                           mTitleLayout;



	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_base_list);
		mRecyclerView = findViewById(R.id.recycle_view);
		mTitleLayout = findViewById(R.id.rl_title);
		mTitleLayout.setVisibility(View.VISIBLE);
		mFinishBtn = findViewById(R.id.btn_finish);
		mTitleTv = findViewById(R.id.tv_title);
		LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
		mRecyclerView.setLayoutManager(layoutManager);
		mRecyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
		mContentList = setContentList();
		mSelectList = new ArrayList<>();
		for (ItemMultiSelectModel model : mContentList) {
			if(Boolean.TRUE.equals(model.isSelected()))
			{
				mSelectList.add(model);
			}
		}
		mAdapter = getAdapter(mContentList);
		mRecyclerView.setAdapter(mAdapter);
		mRecyclerView.addOnItemTouchListener(new OnRecycleViewItemClickListener(this,
				new AdapterView.OnItemClickListener()
				{
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id)
					{
						ItemMultiSelectModel itemMultiSelectModel = mContentList.get(position);
						Boolean selected = itemMultiSelectModel.isSelected();
						if(selected == null){
							return;
						}
						itemMultiSelectModel.setSelected(!selected);
						if(itemMultiSelectModel.isSelected())
						{
							if(!mSelectList.contains(itemMultiSelectModel))
							{
								mSelectList.add(itemMultiSelectModel);
							}
						}
						else
						{
							mSelectList.remove(itemMultiSelectModel);
						}
						mAdapter.notifyDataSetChanged();
					}
				}));

		mFinishBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onFinishClick(mSelectList);
				finish();
			}
		});
	}
	
	private BaseRecycleAdapter<ItemMultiSelectModel> getAdapter(List<ItemMultiSelectModel> contentList)
	{
		return new BaseRecycleAdapter<ItemMultiSelectModel>(contentList)
		{
			@NonNull
			@Override
			public IRecycleAdapterViewHolder<ItemMultiSelectModel> createRecycleAdapterItem(int type)
			{
					return new MultiSelectHolder();
			}
		};
	}
	
	protected abstract List<ItemMultiSelectModel> setContentList();

	protected abstract void onFinishClick(List<ItemMultiSelectModel> selectModelList);

	
	protected void updateContentList(List<ItemMultiSelectModel> list)
	{
		mAdapter.setData(list);
		mAdapter.notifyDataSetChanged();
	}
	
	@NonNull
	protected List<? extends ItemMultiSelectModel> getContentList()
	{
		return mAdapter.getData();
	}

	
}
