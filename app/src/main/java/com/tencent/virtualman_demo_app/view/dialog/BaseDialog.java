package com.tencent.virtualman_demo_app.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.tencent.virtualman_demo_app.R;

public abstract class BaseDialog extends Dialog {

    public BaseDialog(@NonNull Context context) {
        super(context, R.style.MyDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getLayoutId());

        initView();
        initListener();
    }

    protected abstract int getLayoutId();

    protected void initView() {
    }

    protected void initListener() {
    }

    @Override
    protected void onStart() {
        super.onStart();

        Window window = getWindow();
        if (window == null) return;
        initImmersionBar(window);

        //背景阴影透明度
        WindowManager.LayoutParams windowParams = window.getAttributes();
        windowParams.dimAmount = 0f;
        window.setAttributes(windowParams);
    }

    public void initImmersionBar(Window window){
        int options = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        | View.SYSTEM_UI_FLAG_FULLSCREEN  // 隐藏状态栏
        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION  // 隐藏导航栏

        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN  // 允许视图内容延伸到状态栏区域
        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION  // 允许视图延伸到导航栏区域
        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                );
        window.getDecorView().setSystemUiVisibility(options);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            window.setAttributes(lp);
        }
    }
}
