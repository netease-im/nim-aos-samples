package com.netease.nim.samples.utils;

import android.content.Context;
import android.text.TextUtils;
import android.widget.EditText;
import com.bigkoo.pickerview.builder.TimePickerBuilder;
import java.util.Calendar;

public class TimePickUtils {

	private static final String TAG = "TimePickUtils";

	/**
	 * 显示时间选择器
	 */
	public static void showTimePicker(Context context, EditText editText) {
		Calendar calendar = Calendar.getInstance();
		String time = editText.getText().toString();
		Long currentTime = null;
		if (!TextUtils.isEmpty(time)) {
			try {
				currentTime = Long.parseLong(time);
			} catch (Exception e) {
				LogUtils.e(TAG, "时间格式错误",e);
			}
		}
		if (currentTime != null) {
			calendar.setTimeInMillis(currentTime);
		}

		new TimePickerBuilder(context, (date, view) -> editText.setText(String.valueOf(date.getTime()))).setDate(calendar)
				.setType(new boolean[]{true, true, true, true, true, true})// 默认全部显示
				.setLabel("年", "月", "日", "时", "分", "秒")//默认设置为年月日时分秒
				.build().show();
	}
}
