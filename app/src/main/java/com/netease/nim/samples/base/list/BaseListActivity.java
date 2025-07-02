package com.netease.nim.samples.base.list;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.nim.samples.R;
import com.netease.nim.samples.base.BaseActivity;
import com.netease.nim.samples.base.list.model.ItemModel;
import com.netease.nim.samples.base.list.viewholder.ContentHolder;
import com.netease.nim.samples.base.list.viewholder.HeaderHolder;
import com.netease.nim.samples.base.widget.recycleview.BaseRecycleAdapter;
import com.netease.nim.samples.base.widget.recycleview.IRecycleAdapterViewHolder;
import com.netease.nim.samples.base.widget.recycleview.OnRecycleViewItemClickListener;
import java.util.List;

public abstract class BaseListActivity extends BaseActivity
{
	private RecyclerView                  mRecyclerView;
	private List<ItemModel>               mContentList;
	private BaseRecycleAdapter<ItemModel> mAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_base_list);
		mRecyclerView = findViewById(R.id.recycle_view);
		LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
		mRecyclerView.setLayoutManager(layoutManager);
		mRecyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
		mContentList = setContentList();
		mAdapter = getAdapter(mContentList);
		mRecyclerView.setAdapter(mAdapter);
		mRecyclerView.addOnItemTouchListener(new OnRecycleViewItemClickListener(this,
				new AdapterView.OnItemClickListener()
				{
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id)
					{
						onRecycleViewItemClick(position,mContentList.get(position));
					}
				}));
	}
	
	private BaseRecycleAdapter<ItemModel> getAdapter(List<ItemModel> contentList)
	{
		return new BaseRecycleAdapter<ItemModel>(contentList)
		{
			@NonNull
			@Override
			public IRecycleAdapterViewHolder<ItemModel> createRecycleAdapterItem(int type)
			{
				if(type == ItemModel.TYPE_HEADER)
				{
					return new HeaderHolder();
				}else
				{
					return new ContentHolder();
				}
				
			}
		};
	}
	
	protected abstract List<ItemModel> setContentList();
	
	protected void updateContentList(List<ItemModel> list)
	{
		mAdapter.setData(list);
		mAdapter.notifyDataSetChanged();
	}
	
	protected void onRecycleViewItemClick(int position,ItemModel model)
	{
	
	}
	
	@NonNull
	protected List<? extends ItemModel> getContentList()
	{
		return mAdapter.getData();
	}

	
}
