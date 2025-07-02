package com.netease.nim.samples.base.list.model;


/**
 * @author jintao02
 * @date 2021/7/26 15:10
 */
public class ItemMultiSelectModel extends ItemModel
{
	public static final int TYPE_MULTI_SELECT = 100;

	private Boolean isSelected = false;

	public ItemMultiSelectModel(){
		super();
	}


	public ItemMultiSelectModel(String content) {
		super(content);
	}

	public ItemMultiSelectModel(String content,String tag){super(content,tag);}

	public ItemMultiSelectModel(String content, boolean isSupport) {
		super(content, isSupport);
	}

	public ItemMultiSelectModel(String content, int type) {
		super(content, type);
	}

	public ItemMultiSelectModel(String content, int type, boolean isSupport) {
		super(content, type, isSupport);
	}

	public Boolean isSelected() {
		return isSelected;
	}

	public void setSelected(Boolean selected) {
		isSelected = selected;
	}
}
