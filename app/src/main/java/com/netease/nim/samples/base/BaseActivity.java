package com.netease.nim.samples.base;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.netease.nim.samples.base.widget.LoadingDialog;
import com.netease.nim.samples.utils.StatusBarUtils;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by hzsunyj on 2019-07-03.
 */
public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseActivity";
    private LoadingDialog mLoadingDialog = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupFullScreenWithCutout();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        setupSafeArea();
    }

    private void setupFullScreenWithCutout() {
        Window window = getWindow();

        // 透明状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(Color.TRANSPARENT);
        }

        // 关键：正确设置刘海屏模式
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams lp = window.getAttributes();
            // 使用NEVER模式避免内容被刘海遮挡
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER;
            window.setAttributes(lp);
        }
    }


    private void setupSafeArea() {
        View rootView = findViewById(android.R.id.content);
        if (rootView != null) {
            StatusBarUtils.setupWindowInsets(rootView);
        }
    }

    protected void showToast(int p) {
        Toast.makeText(BaseActivity.this, p, Toast.LENGTH_SHORT).show();
    }

    protected void showToast(String message) {
        Toast.makeText(BaseActivity.this, message, Toast.LENGTH_SHORT).show();
        Log.i(TAG, "showToast: " + message);
    }
    
    protected boolean isLoadingDialogShowing()
    {
        return mLoadingDialog != null && mLoadingDialog.isShowing();
    }
    
    protected void showLoadingDialog()
    {
        try {
            if(mLoadingDialog == null)
            {
                mLoadingDialog = new LoadingDialog();
                mLoadingDialog.show(getSupportFragmentManager());
            }
            else if(!isLoadingDialogShowing())
            {
                mLoadingDialog.show(getSupportFragmentManager());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    protected void dismissLoadingDialog()
    {
        try {
            if(isLoadingDialogShowing())
            {
                mLoadingDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
            mLoadingDialog = null;
        }
    }
    
    public void startActivity(Class<? extends Activity> clazz) {
        startActivity(new Intent(this, clazz));
        
    }

    /**
     * 显示时间选择器
     */
    protected void showTimePicker(EditText editText) {
        Calendar calendar = Calendar.getInstance();
        String time = editText.getText().toString();
        Long currentTime = null;
        if(!TextUtils.isEmpty(time)){
            try {
                currentTime = Long.parseLong(time);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(currentTime != null){
            calendar.setTimeInMillis(currentTime);
        }

        new TimePickerBuilder(this, new OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date, View view) {
                editText.setText(""+date.getTime());
            }
        }).setDate(calendar)
                .setType(new boolean[]{true, true, true, true, true, true})// 默认全部显示
                .setLabel("年", "月", "日", "时", "分", "秒")//默认设置为年月日时分秒
                .build().show();

    }
}
