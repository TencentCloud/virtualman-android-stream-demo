package com.tencent.virtualman_demo_app.view.dialog;

import android.content.DialogInterface;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.tencent.virtualman_demo_app.R;
import com.tencent.virtualman_demo_app.utils.ArmUtils;

public class PreviewPopupDialog extends BaseDialogFragment implements View.OnClickListener {

    private TextView popupTitle;
    private TextView popupContent;
    private TextView popupButton;
    private final String title;
    private final String content;
    private final String button;

    public PreviewPopupDialog(String title, String content, String button) {
        super();

        this.title = title;
        this.content = content;
        this.button = button;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_preview_popup;
    }

    @Override
    protected void initData() {
        super.initData();
        popupTitle.setText(title);
        popupContent.setText(content);
        popupButton.setText(button);

    }

    @Override
    protected void initListener() {
        super.initListener();

        getView().findViewById(R.id.popup_button).setOnClickListener(this);
    }

    @Override
    protected void initView() {
        super.initView();

        popupTitle = getView().findViewById(R.id.popup_title);
        popupContent = getView().findViewById(R.id.popup_content);
        popupButton = getView().findViewById(R.id.popup_button);
        popupContent.setMovementMethod(new ScrollingMovementMethod());
        FrameLayout fl_preview_popup = getView().findViewById(R.id.fl_preview_popup);
        int width = ArmUtils.INSTANCE.getScreenWidth(getContext()) - ArmUtils.INSTANCE.dip2px(getContext(), 80);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, width*4/3);
        params.gravity = Gravity.CENTER;
        fl_preview_popup.setLayoutParams(params);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.popup_button: {
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
