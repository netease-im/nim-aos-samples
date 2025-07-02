package com.netease.nim.samples.base.widget.recycleview;

import android.view.View;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;

/**
 * ViewHolder统一接口
 * @author jintao02
 * @date 2021/7/26 14:24
 */
public interface IRecycleAdapterViewHolder<T>
{
    /**
     * @return item布局文件的layoutId
     */
    @LayoutRes
    int getLayoutResId();

    /**
     * 初始化views
     */
    void bindViews(@NonNull final View root);


    /**
     * 根据数据来设置item的内部views
     *
     * @param t    数据list内部的model
     */
    void setData(@NonNull T t);
}
