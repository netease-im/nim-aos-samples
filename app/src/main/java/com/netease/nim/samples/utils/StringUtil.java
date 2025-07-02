package com.netease.nim.samples.utils;

import android.text.TextUtils;
import androidx.annotation.Nullable;

public class StringUtil {

	private static final String TAG = "StringUtil";

	public static boolean isEmpty(@Nullable CharSequence str) {
		return str == null || str.length() <= 0;
	}

	public static boolean isNotEmpty(@Nullable CharSequence str) {
		return str != null && str.length() > 0;
	}

    public static String getExtension(String filename) {
		if (TextUtils.isEmpty(filename)) {
			return "";
		}

        int pos = filename.lastIndexOf('.');
        if (pos != -1) {
            for (int i = pos + 1; i < filename.length(); ++i) {
                char c = filename.charAt(i);
                if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9'))) {
                    return "";
                }
            }
            return filename.substring(pos + 1, filename.length());
        }
        return "";
    }

}
