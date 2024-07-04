package com.tencent.virtualman_demo_app.view.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.tencent.virtualman_demo_app.R;
import com.tencent.virtualman_demo_app.utils.ArmUtils;
import com.tencent.virtualman_demo_app.utils.GlideLoader;

public class PreviewImageDialog extends BaseDialogFragment implements View.OnClickListener {

    private ImageView ivPreviewImage;
    private final String imageUrl;

    public PreviewImageDialog(String imageUrl) {
        super();

        this.imageUrl = imageUrl;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_preview_image;
    }

    @Override
    protected void initData() {
        super.initData();

        if (getContext() == null) return;
        GlideLoader.INSTANCE.loader(getContext(), ivPreviewImage, imageUrl);
    }

    @Override
    protected void initListener() {
        super.initListener();

        View view = getView();
        if (view == null) return;
        view.findViewById(R.id.cl_preview_image).setOnClickListener(this);
        view.findViewById(R.id.ibtn_preview_image_error).setOnClickListener(this);
    }

    @Override
    protected void initView() {
        super.initView();

        View view = getView();
        Context context = getContext();
        if (view == null || context == null) return;
        ivPreviewImage = view.findViewById(R.id.iv_preview_image);
        FrameLayout fl_preview_image = view.findViewById(R.id.fl_preview_image);
        int width = ArmUtils.INSTANCE.getScreenWidth(context) - ArmUtils.INSTANCE.dip2px(context, 160);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, width*4/3);
        params.gravity = Gravity.CENTER;
        fl_preview_image.setLayoutParams(params);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ibtn_preview_image_error:
            case R.id.cl_preview_image: {
                dismiss();
                break;
            }
        }
    }

    private PreviewVideoDialog.OnDismissListener onDismissListener;

    public interface OnDismissListener {
        void onDismiss();
    }

    public void setOnDismissListener(PreviewVideoDialog.OnDismissListener listener) {
        this.onDismissListener = listener;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null) {
            onDismissListener.onDismiss();
        }
    }


}
