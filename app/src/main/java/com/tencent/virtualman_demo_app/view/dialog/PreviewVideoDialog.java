package com.tencent.virtualman_demo_app.view.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.tencent.virtualman_demo_app.R;
import com.tencent.virtualman_demo_app.utils.ArmUtils;
import com.tencent.virtualman_demo_app.utils.GlideLoader;
import com.tencent.virtualman_demo_app.utils.VideoUtils;
import com.tencent.virtualman_demo_app.view.video.JzvdStdCustom;

import cn.jzvd.JZDataSource;
import cn.jzvd.Jzvd;

public class PreviewVideoDialog extends BaseDialogFragment implements View.OnClickListener {

    private JzvdStdCustom jscPreviewVideo;
    private final String videoUrl;

    public PreviewVideoDialog(String videoUrl) {
        super();

        this.videoUrl = videoUrl;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_preview_video;
    }

    private boolean isInit = false;
    @Override
    public void onResume() {
        super.onResume();

        if (isInit){
            Jzvd.goOnPlayOnResume();
        }else {
            isInit = true;
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        Jzvd.goOnPlayOnPause();
    }

    private OnDismissListener onDismissListener;

    public interface OnDismissListener {
        void onDismiss();
    }

    public void setOnDismissListener(OnDismissListener listener) {
        this.onDismissListener = listener;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null) {
            onDismissListener.onDismiss();
        }
    }

    @Override
    protected void initData() {
        Context context = getContext();
        if (context == null) return;
        String[] wh = VideoUtils.INSTANCE.getPlayInfo(videoUrl);
        int w = ArmUtils.INSTANCE.getScreenWidth(context) - ArmUtils.INSTANCE.dip2px(context, 160);
        int h = w*4/3;
        FrameLayout.LayoutParams layoutParams;
        int relW, relH;
        if (wh[0] != null && wh[1] != null) {
            int mW = Integer.parseInt(wh[0]);
            int mH = Integer.parseInt(wh[1]);
            //判断宽高比
            if (mW >= mH){
                //宽大于高--直接充满宽，高按照比例放大
                relW = w;
                relH = (mW * w)/mH;
            }else {
                if ((float)h/w < (float)mH/mW){
                    //可以填满高，但没有填满宽
                    relW = (mW * h)/ mH;
                    relH = h;
                }else {
                    //填满高的同时并不能完全显示宽--使用满宽
                    relW = w;
                    relH = (mH * w)/ mW;
                }
            }
        }else {
            relW = w;
            relH = h;
        }
        if (relW > w){
            relW = w;
        }
        if (relH > h){
            relH = h;
        }
        layoutParams = new FrameLayout.LayoutParams(relW, relH);
        layoutParams.gravity = Gravity.CENTER;
        jscPreviewVideo.setLayoutParams(layoutParams);
        GlideLoader.INSTANCE.loaderCenterCrop(context, jscPreviewVideo.posterImageView, videoUrl);

        JZDataSource source = new JZDataSource(videoUrl, "");
        source.looping = true;
        jscPreviewVideo.setUp(source, Jzvd.SCREEN_NORMAL);
        jscPreviewVideo.startVideo();
    }

    @Override
    protected void initListener() {
        View view = getView();
        if (view == null) return;
        view.findViewById(R.id.cl_preview_video).setOnClickListener(this);
        view.findViewById(R.id.ibtn_preview_video_error).setOnClickListener(this);
    }

    @Override
    protected void initView() {
        View view = getView();
        Context context = getContext();
        if (view == null || context == null) return;
        jscPreviewVideo = view.findViewById(R.id.jsc_preview_video);
        FrameLayout fl_preview_video = view.findViewById(R.id.fl_preview_video);
        int width = ArmUtils.INSTANCE.getScreenWidth(context) - ArmUtils.INSTANCE.dip2px(context, 80);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, width*4/3);
        params.gravity = Gravity.CENTER;
        fl_preview_video.setLayoutParams(params);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ibtn_preview_video_error:
            case R.id.cl_preview_video: {
                dismiss();
                break;
            }
        }
    }
}
