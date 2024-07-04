package com.tencent.virtualman_demo_app.view.dialog;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.tencent.virtualman_demo_app.R;

public abstract class BaseDialogFragment extends DialogFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(DialogFragment.STYLE_NORMAL, R.style.MyDialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return LinearLayout.inflate(getContext(), getLayoutId(), null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();
        initData();
        initListener();
    }

    protected abstract int getLayoutId();

    protected void initView() {

    }

    protected void initData() {

    }

    protected void initListener() {

    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        if (dialog == null) return;
        Window window = dialog.getWindow();
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
