package com.tencent.virtualman_demo_app.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.tencent.virtualman_demo_app.R;
import com.tencent.virtualman_demo_app.utils.ArmUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * 语音录制的动画效果
 */
public class LineWaveVoiceView extends View {
    private final Paint paint = new Paint();
    private final RectF rectRight = new RectF();//右边波纹矩形的数据，10个矩形复用一个rectF
    private final RectF rectLeft = new RectF();//左边波纹矩形的数据
    private int lineColor;
    private float lineWidth;
    private float imageWidth;

    public LineWaveVoiceView(Context context) {
        super(context);
    }

    public LineWaveVoiceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LineWaveVoiceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(attrs, context);
        resetView(mWaveList, DEFAULT_WAVE_HEIGHT);
    }

    private void initView(AttributeSet attrs, Context context) {
        //获取布局属性里的值
        TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.LineWaveVoiceView);
        lineColor = mTypedArray.getColor(R.styleable.LineWaveVoiceView_voiceLineColor, getContext().getResources().getColor(R.color.blue_theme));
        //默认矩形波纹的宽度，9像素, 原则上从layout的attr获得
        lineWidth = mTypedArray.getDimension(R.styleable.LineWaveVoiceView_voiceLineWidth, 9);
        imageWidth = mTypedArray.getDimension(R.styleable.LineWaveVoiceView_imageWidth, ArmUtils.INSTANCE.dip2px(getContext(), 70));
        mTypedArray.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //获取实际宽高的一半
        int widthCentre = getWidth() / 2;
        int heightCentre = getHeight() / 2;
        paint.setStrokeWidth(0);

        //设置颜色
        paint.setColor(lineColor);
        //填充内部
        paint.setStyle(Paint.Style.FILL);
        //设置抗锯齿
        paint.setAntiAlias(true);
        for (int i = 0; i < 10; i++) {
            rectRight.left = widthCentre + imageWidth / 2 + (1 + 2 * i) * lineWidth;
            rectRight.top = heightCentre - lineWidth * mWaveList.get(i) / 2;
            rectRight.right = widthCentre + imageWidth / 2 + (2 + 2 * i) * lineWidth;
            rectRight.bottom = heightCentre + lineWidth * mWaveList.get(i) / 2;

            //左边矩形
            rectLeft.left = widthCentre - imageWidth / 2 - (2 + 2 * i) * lineWidth;
            rectLeft.top = heightCentre - mWaveList.get(i) * lineWidth / 2;
            rectLeft.right = widthCentre - imageWidth / 2 - (1 + 2 * i) * lineWidth;
            rectLeft.bottom = heightCentre + mWaveList.get(i) * lineWidth / 2;

            canvas.drawRoundRect(rectRight, 11, 11, paint);
            canvas.drawRoundRect(rectLeft, 11, 11, paint);
        }
    }

    private static final int[] DEFAULT_WAVE_HEIGHT = {2, 2, 2, 2, 2, 2, 2, 2, 2, 2};
    private LinkedList<Integer> mWaveList = new LinkedList<>();

    private void resetView(List<Integer> list, int[] array) {
        list.clear();
        for (int anArray : array) {
            list.add(anArray);
        }
    }

    public synchronized void addWaveList(int waveH){
        if (waveH > 8){
            waveH = 8;
        }else if (waveH < 2){
            waveH = 2;
        }
        mWaveList.add(0, waveH);
        mWaveList.removeLast();
        postInvalidate();
    }
}