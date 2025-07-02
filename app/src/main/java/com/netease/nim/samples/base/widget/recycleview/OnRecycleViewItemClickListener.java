package com.netease.nim.samples.base.widget.recycleview;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author jintao02
 * @date 2021/7/26 14:31
 */
public class OnRecycleViewItemClickListener  extends RecyclerView.SimpleOnItemTouchListener {
	
	private AdapterView.OnItemClickListener mListener;
	
	private GestureDetector mGestureDetector;
	
	public OnRecycleViewItemClickListener(Context context, AdapterView.OnItemClickListener listener) {
		mListener = listener;
		mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				return true;
			}
		});
	}
	
	@Override
	public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
		View childView = view.findChildViewUnder(e.getX(), e.getY());
		if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {
			int position = view.getChildAdapterPosition(childView);
			mListener.onItemClick(null, childView, position, position);
			return false;
		}
		return false;
	}
	
}
