package com.netease.nim.samples.base.list.model;


import com.netease.nim.samples.base.widget.recycleview.IRecycleAdapterItem;

/**
 * @author jintao02
 * @date 2021/7/26 15:10
 */
public class ItemModel implements IRecycleAdapterItem
{
	public static final int TYPE_CONTENT = 0;
	public static final int TYPE_HEADER = 1;
	/**
	 * Item的内容
	 */
	private String mContent = "";
	/**
	 * 是否支持点击
	 */
	private boolean mIsSupport = true;
	/**
	 * Item类型
	 */
	private int mType = TYPE_CONTENT;
	/**
	 * Item的标签
	 */
	private String mTag;
	@Override
	public int getItemType()
	{
		return mType;
	}

	public ItemModel(){

	}
	
	public ItemModel(String content)
	{
		mContent = content;
		mTag = content;

	}

	public ItemModel(String content,String tag)
	{
		mContent = content;
		mTag = tag;

	}
	
	public ItemModel(String content, boolean isSupport)
	{
		mContent = content;
		mIsSupport = isSupport;
	}
	
	public ItemModel(String content, int type)
	{
		mContent = content;
		mType = type;
	}
	
	public ItemModel(String content, int type,boolean isSupport)
	{
		mContent = content;
		mIsSupport = isSupport;
		mType = type;
	}
	
	public String getContent()
	{
		return mContent;
	}
	
	public void setContent(String content)
	{
		mContent = content;
	}
	
	public boolean isSupport()
	{
		return mIsSupport;
	}
	
	public void setSupport(boolean support)
	{
		mIsSupport = support;
	}

	public int getType() {
		return mType;
	}

	public void setType(int type) {
		mType = type;
	}

	public String getTag() {
		return mTag;
	}

	public void setTag(String tag) {
		mTag = tag;
	}
}
