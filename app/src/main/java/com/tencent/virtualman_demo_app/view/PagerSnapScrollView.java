package com.tencent.virtualman_demo_app.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;

import com.tencent.virtualman_demo_app.R;

import org.jetbrains.annotations.NotNull;

/**
 * 解决RecycleView嵌套ScrollView滑动冲突
 */
public class PagerSnapScrollView extends NestedScrollView {

    private float maxHeight;

    private boolean mIsBottom = false;
    private boolean mIsTop = false;

    private int mDownY;


    public PagerSnapScrollView(@NonNull @NotNull Context context) {
        this(context, null);
    }

    public PagerSnapScrollView(@NonNull @NotNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PagerSnapScrollView(@NonNull @NotNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initEvent(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        heightMeasureSpec = MeasureSpec.makeMeasureSpec((int) maxHeight, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        postDelayed(new Runnable() {
            @Override
            public void run() {
                setScrollY(1);
            }
        },20);

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (outSide()) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mDownY = (int) ev.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    int endY = (int) ev.getY();
                    if ((mIsBottom && (mDownY > endY))) {
                        setDisallowIntercept(false);
                        return super.dispatchTouchEvent(ev);
                    }
                    if ((mIsTop && mDownY < endY)) {
                        setDisallowIntercept(false);
                        return super.dispatchTouchEvent(ev);
                    }
                    setDisallowIntercept(true);
                    return super.dispatchTouchEvent(ev);
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    return super.dispatchTouchEvent(ev);
            }
            setDisallowIntercept(false);
            return super.dispatchTouchEvent(ev);
        } else {
            return super.dispatchTouchEvent(ev);
        }
    }

    /**
     * 所有子布局高度
     */
    private int getChildHeight() {
        if (getChildCount() == 0) {
            return 0;
        }
        int result = 0;
        for (int i = 0; i < getChildCount(); i++) {
            result = result + getChildAt(i).getMeasuredHeight();
        }
        return result;
    }

    /**
     * 是否有超出内容
     */
    private boolean outSide() {
        return getChildHeight() > maxHeight;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return super.onTouchEvent(ev);
    }


    private void setDisallowIntercept(boolean arg) {
        getParent().requestDisallowInterceptTouchEvent(arg);
    }

    private void initEvent(Context context, AttributeSet attrs) {
        TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.PagerSnapScrollView);
        maxHeight = mTypedArray.getDimension(R.styleable.PagerSnapScrollView_maxHeight, 640);
        mTypedArray.recycle();
        mIsBottom = false;
        mIsTop = false;
        mDownY = 0;

        setOnScrollChangeListener(new OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                try {
                    if (scrollY > oldScrollY) {
                        // 向下滑动
                    }
                    if (scrollY < oldScrollY) {
                        // 向上滑动
                    }
                    if (scrollY == 0) {
                        // 顶部
                        mIsTop = true;
                    } else {
                        mIsTop = false;
                    }
                    if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                        //底部
                        mIsBottom = true;
                    } else {
                        mIsBottom = false;
                    }
                } catch (Exception e) {

                }
            }
        });
    }
}
