package com.tencent.virtualman_demo_app.view.video;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.tencent.virtualman_demo_app.R;

import cn.jzvd.JzvdStd;

public class JzvdStdCustom extends JzvdStd {

    private OnVideoComplete onVideoComplete;

    public JzvdStdCustom(Context context) {
        super(context);
    }

    public JzvdStdCustom(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public int getLayoutId() {
        return R.layout.jz_layout_std_custom;
    }

    @Override
    public void onStateAutoComplete() {
        super.onStateAutoComplete();

        if (onVideoComplete != null) {
            onVideoComplete.onComplete();
        }
    }

    public void setOnVideoComplete(OnVideoComplete onVideoComplete) {
        this.onVideoComplete = onVideoComplete;
    }

    public void onClickUiToggle() {//这是事件
        if (bottomContainer.getVisibility() != View.VISIBLE) {
            setSystemTimeAndBattery();
            clarity.setText(jzDataSource.getCurrentKey().toString());
        }
        if (state == STATE_PREPARING) {
            changeUiToPreparing();
            if (bottomContainer.getVisibility() == View.VISIBLE) {
            } else {
                setSystemTimeAndBattery();
            }
        } else if (state == STATE_PLAYING) {
            startButton.performClick();
        } else if (state == STATE_PAUSE) {
            startButton.performClick();
        }
    }

    public interface OnVideoComplete{
        void onComplete();
    }

    @Override
    public void updateStartImage() {
        if (state == STATE_PLAYING) {
            startButton.setVisibility(VISIBLE);
            replayTextView.setVisibility(GONE);
        } else if (state == STATE_ERROR) {
            startButton.setVisibility(INVISIBLE);
            replayTextView.setVisibility(GONE);
        } else if (state == STATE_AUTO_COMPLETE) {
            startButton.setVisibility(VISIBLE);
            replayTextView.setVisibility(VISIBLE);
        } else {
            replayTextView.setVisibility(GONE);
        }
    }
}
