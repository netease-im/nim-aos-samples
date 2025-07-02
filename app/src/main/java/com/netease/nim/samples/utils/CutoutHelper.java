package com.netease.nim.samples.utils;

import android.app.Activity;
import android.os.Build;
import android.view.DisplayCutout;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class CutoutHelper {
    
    // 为正常显示模式设置安全区域
    public static void setupSafeAreaForNormalScreen(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            View rootView = activity.findViewById(android.R.id.content);
            
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (view, insets) -> {
                Insets cutoutInsets = insets.getInsets(WindowInsetsCompat.Type.displayCutout());
                
                // 只在有刘海屏时才添加额外间距
                if (cutoutInsets.top > 0) {
                    // 获取当前的padding
                    int currentTop = view.getPaddingTop();
                    
                    // 添加刘海屏的安全距离
                    view.setPadding(
                        view.getPaddingLeft(),
                        Math.max(currentTop, cutoutInsets.top),
                        view.getPaddingRight(),
                        view.getPaddingBottom()
                    );
                }
                
                return insets;
            });
        }
    }
    
    // 为特定View设置安全区域margin
    public static void setupSafeAreaMargin(View targetView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ViewCompat.setOnApplyWindowInsetsListener(targetView, (view, insets) -> {
                Insets cutoutInsets = insets.getInsets(WindowInsetsCompat.Type.displayCutout());
                
                if (cutoutInsets.top > 0) {
                    ViewGroup.MarginLayoutParams params =
                        (ViewGroup.MarginLayoutParams) view.getLayoutParams();
                    
                    params.topMargin = Math.max(params.topMargin, cutoutInsets.top);
                    view.setLayoutParams(params);
                }
                
                return insets;
            });
        }
    }
    
    // 检查是否需要刘海屏适配
    public static boolean needsCutoutHandling(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowInsets insets = activity.getWindow().getDecorView().getRootWindowInsets();
            if (insets != null) {
                DisplayCutout cutout = insets.getDisplayCutout();
                return cutout != null && cutout.getSafeInsetTop() > 0;
            }
        }
        return false;
    }
    
    // 获取刘海屏的安全顶部距离
    public static int getSafeCutoutTop(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowInsets insets = activity.getWindow().getDecorView().getRootWindowInsets();
            if (insets != null) {
                DisplayCutout cutout = insets.getDisplayCutout();
                if (cutout != null) {
                    return cutout.getSafeInsetTop();
                }
            }
        }
        return 0;
    }
}